package com.mindx360.mcp.mcp;

import com.mindx360.mcp.mcp.model.McpToolDefinition;
import com.mindx360.mcp.tool.annotation.ToolParam;

import java.lang.reflect.Method;
import java.lang.reflect.Parameter;
import java.util.Map;

/**
 * Immutable record that binds a Spring bean instance and its reflective
 * {@link Method} to the pre-built {@link McpToolDefinition} metadata.
 *
 * <p>Kept package-private; callers interact with
 * {@link McpToolRegistry} and {@link McpRequestHandler} only.
 */
record ToolEntry(Object bean, Method method, McpToolDefinition definition) {

    /**
     * Reflectively invokes the tool method, converting raw argument values
     * (from the JSON-RPC {@code arguments} map) into the Java types that the
     * method parameters require.
     *
     * @param arguments map of parameter name → value as received from Claude
     * @return whatever the tool method returns (usually a List of entities)
     * @throws RuntimeException wrapping any reflection-level exception
     */
    Object invoke(final Map<String, Object> arguments) {
        final Parameter[] params = method.getParameters();
        final Object[]    args   = new Object[params.length];

        for (int i = 0; i < params.length; i++) {
            final ToolParam toolParam = params[i].getAnnotation(ToolParam.class);
            if (toolParam != null) {
                final Object rawValue = arguments == null ? null : arguments.get(toolParam.name());
                args[i] = convertArg(rawValue, params[i].getType());
            }
        }

        try {
            return method.invoke(bean, args);
        } catch (Exception e) {
            throw new RuntimeException(
                "Failed to invoke tool '%s': %s".formatted(definition.getName(), e.getMessage()),
                e
            );
        }
    }

    // -----------------------------------------------------------------------
    // Private helpers
    // -----------------------------------------------------------------------

    /**
     * Converts a raw JSON-deserialised value to the Java type expected by the
     * method parameter.
     *
     * <p>Jackson deserialises JSON numbers as {@code Integer}, {@code Long},
     * or {@code Double} depending on the magnitude, so we normalise via
     * {@link Number} where appropriate.
     */
    private Object convertArg(final Object value, final Class<?> targetType) {
        if (value == null) {
            return null;
        }

        if (String.class.equals(targetType)) {
            return value.toString();
        }

        if (Double.class.equals(targetType) || double.class.equals(targetType)) {
            return (value instanceof Number n) ? n.doubleValue()
                                               : Double.parseDouble(value.toString());
        }

        if (Long.class.equals(targetType) || long.class.equals(targetType)) {
            return (value instanceof Number n) ? n.longValue()
                                               : Long.parseLong(value.toString());
        }

        if (Integer.class.equals(targetType) || int.class.equals(targetType)) {
            return (value instanceof Number n) ? n.intValue()
                                               : Integer.parseInt(value.toString());
        }

        if (Boolean.class.equals(targetType) || boolean.class.equals(targetType)) {
            return (value instanceof Boolean b) ? b : Boolean.parseBoolean(value.toString());
        }

        return value;
    }
}
