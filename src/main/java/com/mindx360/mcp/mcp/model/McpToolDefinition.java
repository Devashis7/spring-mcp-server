package com.mindx360.mcp.mcp.model;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.List;
import java.util.Map;

/**
 * Describes a single tool in the format required by the MCP {@code tools/list}
 * response.
 *
 * <p>Claude Desktop reads these definitions to understand what tools are
 * available and when to invoke them.  The {@link #inputSchema} must be a valid
 * JSON Schema (Draft 7) object.
 *
 * <p>Example serialised form:
 * <pre>{@code
 * {
 *   "name": "getEmployeesByDepartment",
 *   "description": "Retrieves all employees who belong to a specific department.",
 *   "inputSchema": {
 *     "type": "object",
 *     "properties": {
 *       "department": {
 *         "type": "string",
 *         "description": "The department name, e.g. 'IT'."
 *       }
 *     },
 *     "required": ["department"]
 *   }
 * }
 * }</pre>
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@JsonInclude(JsonInclude.Include.NON_NULL)
public class McpToolDefinition {

    /** Unique tool identifier matching the {@code name} in {@code @Tool}. */
    private String name;

    /** Natural-language description consumed by Claude. */
    private String description;

    /**
     * JSON Schema object describing the tool's input parameters.
     * Built dynamically by {@link com.mindx360.mcp.mcp.McpToolRegistry}.
     */
    private InputSchema inputSchema;



    // -----------------------------------------------------------------------
    // Inner schema types
    // -----------------------------------------------------------------------

    /**
     * Top-level JSON Schema wrapper; always {@code "object"} for MCP tools.
     */
    @Getter
    @Setter
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    @JsonInclude(JsonInclude.Include.NON_NULL)
    public static class InputSchema {

        /** Always {@code "object"}. */
        @Builder.Default
        private String type = "object";

        /** Map of parameter name → parameter schema. */
        private Map<String, ParameterSchema> properties;

        /** Names of mandatory parameters. */
        private List<String> required;
    }

    /**
     * Schema for a single tool parameter.
     */
    @Getter
    @Setter
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    @JsonInclude(JsonInclude.Include.NON_NULL)
    public static class ParameterSchema {

        /** JSON Schema type: {@code "string"}, {@code "number"}, etc. */
        private String type;

        /** Description shown to Claude when it must decide what value to pass. */
        private String description;
    }
}
