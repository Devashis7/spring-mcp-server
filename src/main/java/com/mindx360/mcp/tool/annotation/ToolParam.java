package com.mindx360.mcp.tool.annotation;

import java.lang.annotation.*;

/**
 * Documents a single parameter of an MCP tool method.
 *
 * <p>Applied to method parameters to carry metadata used when building the
 * JSON Schema {@code properties} block for that tool.  Claude reads the
 * {@link #description()} to understand what value to supply.
 *
 * <p>Usage example:
 * <pre>{@code
 * public List<Employee> getEmployeesByDepartment(
 *     @ToolParam(name = "department",
 *                description = "The department name, e.g. 'IT' or 'HR'",
 *                required = true) String department) { ... }
 * }</pre>
 */
@Target(ElementType.PARAMETER)
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface ToolParam {

    /** The parameter name as it will appear in the JSON Schema. */
    String name();

    /**
     * Natural-language description of what the parameter represents and what
     * values are acceptable.
     */
    String description();

    /**
     * JSON Schema type for this parameter.  Defaults to {@code "string"}.
     * Supported values: {@code "string"}, {@code "number"}, {@code "boolean"}.
     */
    String type() default "string";

    /**
     * Whether the parameter is required by Claude when calling this tool.
     * Maps to the {@code required} array in the JSON Schema.
     */
    boolean required() default true;
}
