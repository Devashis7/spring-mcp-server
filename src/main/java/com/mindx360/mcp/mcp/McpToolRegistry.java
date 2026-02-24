package com.mindx360.mcp.mcp;

import com.mindx360.mcp.mcp.model.McpToolDefinition;
import com.mindx360.mcp.tool.McpToolProvider;
import com.mindx360.mcp.tool.annotation.Tool;
import com.mindx360.mcp.tool.annotation.ToolParam;
import jakarta.annotation.PostConstruct;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.aop.support.AopUtils;
import org.springframework.stereotype.Component;

import java.lang.reflect.Method;
import java.lang.reflect.Parameter;
import java.util.*;

/**
 * Discovers and holds all MCP-callable tools.
 *
 * <p>Spring injects every {@link McpToolProvider} bean via collection injection
 * ({@code List<McpToolProvider>}).  At startup (via {@link PostConstruct}),
 * this registry scans each provider for methods annotated with {@link Tool},
 * builds JSON Schema metadata ({@link McpToolDefinition}), and stores the
 * entry in an insertion-ordered map keyed by tool name.
 *
 * <p>Using collection injection instead of full {@code ApplicationContext}
 * scanning avoids the circular-dependency issue that would occur if any bean
 * that depends on {@code McpToolRegistry} were instantiated during the
 * registry's own initialisation.
 */
@Component
public class McpToolRegistry {

    private static final Logger log = LoggerFactory.getLogger(McpToolRegistry.class);

    /** Preserves registration order for deterministic tools/list responses. */
    private final Map<String, ToolEntry> registry = new LinkedHashMap<>();

    /** All beans that implement {@link McpToolProvider} – injected by Spring. */
    private final List<McpToolProvider> toolProviders;

    public McpToolRegistry(final List<McpToolProvider> toolProviders) {
        this.toolProviders = toolProviders;
    }

    // -----------------------------------------------------------------------
    // Lifecycle
    // -----------------------------------------------------------------------

    /**
     * Scans every {@link McpToolProvider} bean for {@link Tool}-annotated
     * methods and registers them.  Called automatically by Spring after
     * dependency injection completes.
     */
    @PostConstruct
    public void discoverTools() {
        for (final McpToolProvider provider : toolProviders) {
            scanBeanForTools(provider);
        }
        log.info("[McpToolRegistry] Registered {} tool(s): {}",
                registry.size(), registry.keySet());
    }

    // -----------------------------------------------------------------------
    // Public API
    // -----------------------------------------------------------------------

    /**
     * Returns an ordered list of tool definitions for the {@code tools/list}
     * response.
     *
     * @return unmodifiable list of all registered tool definitions
     */
    public List<McpToolDefinition> getToolDefinitions() {
        return registry.values().stream()
                .map(ToolEntry::definition)
                .toList();
    }

    /**
     * Invokes the tool identified by {@code toolName} with the supplied
     * argument map and returns the result.
     *
     * @param toolName  the exact tool name
     * @param arguments map of parameter name → value from the MCP request
     * @return the result of the tool method invocation
     * @throws IllegalArgumentException if no tool with that name is registered
     */
    public Object invoke(final String toolName, final Map<String, Object> arguments) {
        final ToolEntry entry = registry.get(toolName);
        if (entry == null) {
            throw new IllegalArgumentException("Unknown tool: " + toolName);
        }
        return entry.invoke(arguments);
    }

    /**
     * Returns {@code true} if a tool with the given name is registered.
     *
     * @param toolName tool name to check
     * @return {@code true} if present
     */
    public boolean hasTool(final String toolName) {
        return registry.containsKey(toolName);
    }

    // -----------------------------------------------------------------------
    // Private helpers
    // -----------------------------------------------------------------------

    private void scanBeanForTools(final Object bean) {
        // Use the actual class, not a CGLIB / JDK proxy, to discover @Tool annotations
        final Class<?> targetClass = AopUtils.getTargetClass(bean);

        for (final Method method : targetClass.getMethods()) {
            final Tool toolAnnotation = method.getAnnotation(Tool.class);
            if (toolAnnotation == null) {
                continue;
            }

            final McpToolDefinition definition = buildDefinition(toolAnnotation, method);
            registry.put(toolAnnotation.name(), new ToolEntry(bean, method, definition));

            log.debug("[McpToolRegistry] Registered tool '{}' → {}#{}",
                    toolAnnotation.name(), targetClass.getSimpleName(), method.getName());
        }
    }

    /**
     * Builds a {@link McpToolDefinition} from the {@link Tool} annotation and
     * any {@link ToolParam} annotations on the method parameters.
     */
    private McpToolDefinition buildDefinition(final Tool toolAnnotation, final Method method) {
        final Map<String, McpToolDefinition.ParameterSchema> properties = new LinkedHashMap<>();
        final List<String> requiredParams = new ArrayList<>();

        for (final Parameter param : method.getParameters()) {
            final ToolParam toolParam = param.getAnnotation(ToolParam.class);
            if (toolParam == null) {
                continue;
            }

            properties.put(
                toolParam.name(),
                McpToolDefinition.ParameterSchema.builder()
                        .type(toolParam.type())
                        .description(toolParam.description())
                        .build()
            );

            if (toolParam.required()) {
                requiredParams.add(toolParam.name());
            }
        }

        final McpToolDefinition.InputSchema inputSchema = McpToolDefinition.InputSchema.builder()
                .type("object")
                .properties(properties.isEmpty() ? null : properties)
                .required(requiredParams.isEmpty() ? null : requiredParams)
                .build();

        return McpToolDefinition.builder()
                .name(toolAnnotation.name())
                .description(toolAnnotation.description())
                .inputSchema(inputSchema)
                .build();
    }
}
