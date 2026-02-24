package com.mindx360.mcp.mcp;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.mindx360.mcp.mcp.model.McpRequest;
import com.mindx360.mcp.mcp.model.McpResponse;
import com.mindx360.mcp.mcp.model.McpToolDefinition;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.util.*;

/**
 * Processes decoded MCP / JSON-RPC 2.0 requests and produces responses.
 *
 * <p>This class is the central dispatcher.  It understands the MCP method
 * surface used by Claude Desktop:
 * <ul>
 *   <li>{@code initialize}                  – handshake</li>
 *   <li>{@code notifications/initialized}   – fire-and-forget, no reply</li>
 *   <li>{@code tools/list}                  – enumerate available tools</li>
 *   <li>{@code tools/call}                  – execute a tool</li>
 * </ul>
 *
 * <p>All MCP-specific concerns (protocol version, capability negotiation,
 * content-block wrapping) live here; business logic stays in the service and
 * tool layers.
 */
@Component
public class McpRequestHandler {

    private static final Logger log = LoggerFactory.getLogger(McpRequestHandler.class);

    /** MCP protocol version this server implements. */
    private static final String PROTOCOL_VERSION = "2024-11-05";

    // JSON-RPC 2.0 standard error codes
    private static final int ERR_METHOD_NOT_FOUND = -32601;
    private static final int ERR_INVALID_PARAMS   = -32602;
    private static final int ERR_INTERNAL         = -32603;

    private final McpToolRegistry toolRegistry;
    private final ObjectMapper    objectMapper;

    public McpRequestHandler(final McpToolRegistry toolRegistry,
                             final ObjectMapper objectMapper) {
        this.toolRegistry = toolRegistry;
        this.objectMapper = objectMapper;
    }

    // -----------------------------------------------------------------------
    // Main dispatcher
    // -----------------------------------------------------------------------

    /**
     * Dispatches the decoded {@link McpRequest} to the appropriate handler.
     *
     * @param request the decoded request
     * @return the response to send back, or {@code null} for notifications
     *         (fire-and-forget messages that must not be answered)
     */
    public McpResponse handle(final McpRequest request) {
        if (request.getMethod() == null) {
            log.warn("[McpRequestHandler] Received request with null method");
            return McpResponse.error(request.getId(), ERR_INVALID_PARAMS, "method is required");
        }

        log.debug("[McpRequestHandler] Handling method='{}'", request.getMethod());

        return switch (request.getMethod()) {
            case "initialize"               -> handleInitialize(request);
            case "notifications/initialized" -> null;   // No response for notifications
            case "tools/list"               -> handleToolsList(request);
            case "tools/call"               -> handleToolsCall(request);
            default -> {
                log.warn("[McpRequestHandler] Unknown method: {}", request.getMethod());
                yield McpResponse.error(
                    request.getId(),
                    ERR_METHOD_NOT_FOUND,
                    "Method not found: " + request.getMethod()
                );
            }
        };
    }

    // -----------------------------------------------------------------------
    // initialize
    // -----------------------------------------------------------------------

    /**
     * Responds to the MCP handshake with server capabilities.
     *
     * <p>Claude Desktop uses the {@code serverInfo} to display the server in
     * its UI and the {@code capabilities} block to know which MCP features are
     * supported.
     */
    private McpResponse handleInitialize(final McpRequest request) {
        final Map<String, Object> serverInfo = Map.of(
            "name",    "spring-mcp-server",
            "version", "1.0.0"
        );

        final Map<String, Object> capabilities = Map.of(
            "tools", Map.of()   // We support the tools capability
        );

        final Map<String, Object> result = new LinkedHashMap<>();
        result.put("protocolVersion", PROTOCOL_VERSION);
        result.put("capabilities",    capabilities);
        result.put("serverInfo",      serverInfo);

        log.info("[McpRequestHandler] Initialized MCP session – client={}",
                extractClientName(request));

        return McpResponse.success(request.getId(), result);
    }

    // -----------------------------------------------------------------------
    // tools/list
    // -----------------------------------------------------------------------

    /**
     * Returns the list of all registered tools to Claude Desktop.
     * Claude reads these definitions to decide when and how to call each tool.
     */
    private McpResponse handleToolsList(final McpRequest request) {
        final List<McpToolDefinition> definitions = toolRegistry.getToolDefinitions();
        final Map<String, Object> result = Map.of("tools", definitions);

        log.debug("[McpRequestHandler] tools/list – returning {} tool(s)", definitions.size());
        return McpResponse.success(request.getId(), result);
    }

    // -----------------------------------------------------------------------
    // tools/call
    // -----------------------------------------------------------------------

    /**
     * Executes the named tool with the arguments provided by Claude and wraps
     * the result in the MCP {@code content} block format.
     *
     * <p>Expected params shape:
     * <pre>{@code
     * { "name": "getEmployeesByDepartment", "arguments": { "department": "IT" } }
     * }</pre>
     */
    private McpResponse handleToolsCall(final McpRequest request) {
        final JsonNode params = request.getParams();

        if (params == null || !params.has("name")) {
            return McpResponse.error(request.getId(), ERR_INVALID_PARAMS,
                    "tools/call requires 'name' in params");
        }

        final String toolName  = params.get("name").asText();
        final JsonNode argsNode = params.has("arguments") ? params.get("arguments") : null;

        if (!toolRegistry.hasTool(toolName)) {
            return McpResponse.error(request.getId(), ERR_METHOD_NOT_FOUND,
                    "Unknown tool: " + toolName);
        }

        try {
            // Convert JsonNode arguments → plain Map<String, Object>
            final Map<String, Object> arguments = deserialiseArguments(argsNode);

            log.info("[McpRequestHandler] Calling tool='{}' with args={}", toolName, arguments);
            final Object toolResult = toolRegistry.invoke(toolName, arguments);

            // Serialise the tool result to a JSON string for the text content block
            final String resultJson = objectMapper.writerWithDefaultPrettyPrinter()
                                                  .writeValueAsString(toolResult);

            // MCP content block wrapping
            final List<Map<String, Object>> content = List.of(
                Map.of("type", "text", "text", resultJson)
            );

            final Map<String, Object> result = new LinkedHashMap<>();
            result.put("content",  content);
            result.put("isError",  false);

            return McpResponse.success(request.getId(), result);

        } catch (IllegalArgumentException e) {
            log.warn("[McpRequestHandler] Invalid argument for tool '{}': {}", toolName, e.getMessage());
            return McpResponse.error(request.getId(), ERR_INVALID_PARAMS, e.getMessage());

        } catch (Exception e) {
            log.error("[McpRequestHandler] Internal error executing tool '{}'", toolName, e);
            return McpResponse.error(request.getId(), ERR_INTERNAL,
                    "Tool execution failed: " + e.getMessage());
        }
    }

    // -----------------------------------------------------------------------
    // Utility helpers
    // -----------------------------------------------------------------------

    /**
     * Converts a {@link JsonNode} arguments object into a plain
     * {@code Map<String, Object>} for the reflection-based invocation path.
     */
    @SuppressWarnings("unchecked")
    private Map<String, Object> deserialiseArguments(final JsonNode argsNode) {
        if (argsNode == null || argsNode.isNull()) {
            return Collections.emptyMap();
        }
        return objectMapper.convertValue(argsNode, Map.class);
    }

    /**
     * Extracts the {@code clientInfo.name} field from an {@code initialize}
     * request for logging purposes.
     */
    private String extractClientName(final McpRequest request) {
        try {
            final JsonNode params = request.getParams();
            if (params != null && params.has("clientInfo")) {
                return params.get("clientInfo").path("name").asText("unknown");
            }
        } catch (Exception ignored) {
            // non-critical
        }
        return "unknown";
    }
}
