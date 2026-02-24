package com.mindx360.mcp.mcp.model;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.databind.JsonNode;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * Represents an outbound JSON-RPC 2.0 response sent back to Claude Desktop.
 *
 * <p>A successful response contains a non-null {@link #result}; an error
 * response contains a non-null {@link #error}.  Exactly one of the two must be
 * present per JSON-RPC 2.0 specification.
 *
 * <p>The {@link #id} must mirror the {@code id} from the originating request
 * so that the client can match replies to requests.
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@JsonInclude(JsonInclude.Include.NON_NULL)
public class McpResponse {

    /** Always {@code "2.0"}. */
    @Builder.Default
    private String jsonrpc = "2.0";

    /** Mirrors the request {@code id}. Null for notifications. */
    private JsonNode id;

    /**
     * The result payload on success.  Shape varies by method:
     * <ul>
     *   <li>{@code initialize} → server capabilities object</li>
     *   <li>{@code tools/list} → {@code { "tools": [...] }}</li>
     *   <li>{@code tools/call} → {@code { "content": [...] }}</li>
     * </ul>
     */
    private Object result;

    /**
     * Populated instead of {@link #result} when the request cannot be
     * fulfilled.  Uses the standard JSON-RPC error object shape.
     */
    private McpError error;

    // -----------------------------------------------------------------------
    // Factory helpers
    // -----------------------------------------------------------------------

    /**
     * Creates a success response with the standard JSON-RPC envelope.
     *
     * @param id     the request id to echo back
     * @param result the result payload
     * @return a fully assembled success response
     */
    public static McpResponse success(final JsonNode id, final Object result) {
        return McpResponse.builder()
                .jsonrpc("2.0")
                .id(id)
                .result(result)
                .build();
    }

    /**
     * Creates an error response with the standard JSON-RPC envelope.
     *
     * @param id      the request id to echo back (may be null for parse errors)
     * @param code    JSON-RPC error code
     * @param message human-readable error message
     * @return a fully assembled error response
     */
    public static McpResponse error(final JsonNode id, final int code, final String message) {
        return McpResponse.builder()
                .jsonrpc("2.0")
                .id(id)
                .error(new McpError(code, message))
                .build();
    }
}
