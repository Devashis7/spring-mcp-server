package com.mindx360.mcp.tool.annotation;

import java.lang.annotation.*;

/**
 * Marks a method as an MCP-callable tool.
 *
 * <p>When the {@code McpToolRegistry} scans a bean annotated with this
 * marker, it reads {@link #name()} and {@link #description()} to build the
 * JSON schema that is sent to Claude Desktop on a {@code tools/list} request.
 *
 * <p>Usage example:
 * <pre>{@code
 * @Tool(name = "getAllEmployees", description = "Retrieves all employees")
 * public List<Employee> getAllEmployees() { ... }
 * }</pre>
 */
@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface Tool {

    /**
     * Unique tool name exposed to the MCP client.
     * Must match the name Claude Desktop will use in {@code tools/call}.
     */
    String name();

    /**
     * Human-readable description that Claude uses to decide when to call this
     * tool.  Write it in natural language; it becomes the {@code description}
     * field of the JSON Schema object.
     */
    String description();
}
