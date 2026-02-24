package com.mindx360.mcp.mcp;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.mindx360.mcp.mcp.model.McpRequest;
import com.mindx360.mcp.mcp.model.McpResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.PrintStream;
import java.nio.charset.StandardCharsets;

/**
 * STDIO transport layer for the MCP server.
 *
 * <p>Implements {@link CommandLineRunner} so Spring Boot executes
 * {@link #run(String...)} after the application context is fully started.
 * The runner enters a blocking read-loop on {@link System#in}, processing
 * one newline-delimited JSON-RPC message per iteration and writing the
 * serialised response to {@link System#out}.
 *
 * <h3>STDIO contract with Claude Desktop</h3>
 * <ul>
 *   <li>Each MCP message is exactly one line of UTF-8 JSON followed by
 *       {@code \n}.</li>
 *   <li><strong>stdout must contain only MCP JSON responses.</strong>  All
 *       diagnostic output goes to a log file (see application.properties).</li>
 *   <li>The process exits when stdin reaches EOF (Claude Desktop closed the
 *       connection).</li>
 * </ul>
 *
 * <h3>Thread model</h3>
 * The read-loop runs on the {@code main} thread (which is appropriate for a
 * foreground STDIO process).  Spring's web context keeps the HTTP server active
 * in the background for the H2 console but does not interfere with MCP I/O.
 */
@Component
public class McpRunner implements CommandLineRunner {

    private static final Logger log = LoggerFactory.getLogger(McpRunner.class);

    private final McpRequestHandler requestHandler;
    private final ObjectMapper      objectMapper;

    public McpRunner(final McpRequestHandler requestHandler,
                     final ObjectMapper objectMapper) {
        this.requestHandler = requestHandler;
        this.objectMapper   = objectMapper;
    }

    // -----------------------------------------------------------------------
    // CommandLineRunner
    // -----------------------------------------------------------------------

    /**
     * Enters the STDIO read-loop. Blocks until stdin is closed.
     *
     * @param args command-line arguments (not used)
     */
    @Override
    public void run(final String... args) {
        log.info("[McpRunner] MCP STDIO transport started – waiting for messages…");

        // Wrap stdout in a UTF-8 PrintStream without auto-flush so we control
        // exactly when each response line is flushed.
        final PrintStream out = new PrintStream(System.out, false, StandardCharsets.UTF_8);

        try (BufferedReader reader = new BufferedReader(
                new InputStreamReader(System.in, StandardCharsets.UTF_8))) {

            String line;
            while ((line = reader.readLine()) != null) {
                final String trimmed = line.trim();
                if (trimmed.isEmpty()) {
                    continue;   // skip blank lines
                }

                processMessage(trimmed, out);
            }

        } catch (Exception e) {
            log.error("[McpRunner] Fatal error in STDIO loop", e);
        }

        log.info("[McpRunner] stdin closed – MCP server shutting down.");
    }

    // -----------------------------------------------------------------------
    // Per-message processing
    // -----------------------------------------------------------------------

    /**
     * Parses one JSON line, dispatches it, and writes the response.
     *
     * @param rawMessage the raw JSON string read from stdin
     * @param out         the output stream to write the response to
     */
    private void processMessage(final String rawMessage, final PrintStream out) {
        log.debug("[McpRunner] ← RECV: {}", rawMessage);

        McpRequest request;

        // --- 1. Parse ---
        try {
            request = objectMapper.readValue(rawMessage, McpRequest.class);
        } catch (JsonProcessingException e) {
            log.warn("[McpRunner] Could not parse message: {}", e.getMessage());
            // Respond with a parse-error; id is null because we could not read it
            writeResponse(McpResponse.error(null, -32700, "Parse error: " + e.getMessage()), out);
            return;
        }

        // --- 2. Notifications have no id – no response required ---
        if (request.getId() == null && isNotification(request.getMethod())) {
            log.debug("[McpRunner] Notification '{}' received – no response sent.",
                    request.getMethod());
            try {
                // Still dispatch so the handler can update internal state
                requestHandler.handle(request);
            } catch (Exception ignored) { }
            return;
        }

        // --- 3. Dispatch ---
        McpResponse response;
        try {
            response = requestHandler.handle(request);
        } catch (Exception e) {
            log.error("[McpRunner] Unhandled exception handling method '{}'",
                    request.getMethod(), e);
            response = McpResponse.error(request.getId(), -32603,
                    "Internal error: " + e.getMessage());
        }

        // --- 4. Send response (null means the handler intentionally produced none) ---
        if (response != null) {
            writeResponse(response, out);
        }
    }

    // -----------------------------------------------------------------------
    // Output
    // -----------------------------------------------------------------------

    /**
     * Serialises the {@link McpResponse} to a single JSON line and flushes it
     * to stdout.
     */
    private void writeResponse(final McpResponse response, final PrintStream out) {
        try {
            final String json = objectMapper.writeValueAsString(response);
            log.debug("[McpRunner] → SEND: {}", json);
            out.println(json);
            out.flush();
        } catch (JsonProcessingException e) {
            log.error("[McpRunner] Failed to serialise response", e);
        }
    }

    // -----------------------------------------------------------------------
    // Helpers
    // -----------------------------------------------------------------------

    /** Returns {@code true} for methods that are notifications (no response). */
    private boolean isNotification(final String method) {
        return method != null && method.startsWith("notifications/");
    }
}
