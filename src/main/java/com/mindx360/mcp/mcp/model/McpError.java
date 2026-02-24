package com.mindx360.mcp.mcp.model;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * JSON-RPC 2.0 error object embedded inside {@link McpResponse#getError()}.
 *
 * <p>Standard error codes from the JSON-RPC 2.0 specification:
 * <ul>
 *   <li>{@code -32700} – Parse error</li>
 *   <li>{@code -32600} – Invalid request</li>
 *   <li>{@code -32601} – Method not found</li>
 *   <li>{@code -32602} – Invalid params</li>
 *   <li>{@code -32603} – Internal error</li>
 * </ul>
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class McpError {

    /** JSON-RPC 2.0 standard error code. */
    private int code;

    /** Human-readable error message forwarded to Claude Desktop. */
    private String message;
}
