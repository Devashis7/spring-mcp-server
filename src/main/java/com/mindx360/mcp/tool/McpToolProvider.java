package com.mindx360.mcp.tool;

/**
 * Marker interface for Spring beans that expose MCP-callable tools.
 *
 * <p>Any {@code @Component} that implements this interface will be
 * automatically collected by {@link com.mindx360.mcp.mcp.McpToolRegistry}
 * via Spring's standard collection-injection ({@code List<McpToolProvider>}).
 *
 * <p>This eliminates the need to scan the entire application context and
 * prevents circular-dependency issues between the registry and other beans
 * that depend on it.
 */
public interface McpToolProvider {
    // marker – no methods required
}
