package com.mindx360.mcp.mcp.model;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.databind.JsonNode;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;

/**
 * Represents an inbound JSON-RPC 2.0 request from Claude Desktop.
 *
 * <p>The MCP protocol wraps every client message in this envelope.  The
 * {@code method} field determines how the request is dispatched:
 * <ul>
 *   <li>{@code initialize}          – sent once on connection</li>
 *   <li>{@code notifications/initialized} – fire-and-forget confirmation</li>
 *   <li>{@code tools/list}          – list available tools</li>
 *   <li>{@code tools/call}          – invoke a specific tool</li>
 * </ul>
 *
 * <p>{@code params} is intentionally typed as {@link JsonNode} to accommodate
 * the varying shapes of different methods without requiring a class hierarchy.
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@ToString
@JsonIgnoreProperties(ignoreUnknown = true)
@JsonInclude(JsonInclude.Include.NON_NULL)
public class McpRequest {

    /** JSON-RPC protocol version; always {@code "2.0"}. */
    private String jsonrpc;

    /**
     * Request identifier echoed back in the response so the client can
     * correlate async replies.  May be a number or a string per the spec.
     * Null for notifications (fire-and-forget messages).
     */
    private JsonNode id;

    /**
     * The RPC method name, e.g. {@code "initialize"}, {@code "tools/list"},
     * {@code "tools/call"}.
     */
    private String method;

    /**
     * Method-specific parameters.  Parsed lazily via Jackson so each handler
     * can deserialise only the fields it needs.
     */
    private JsonNode params;
}
