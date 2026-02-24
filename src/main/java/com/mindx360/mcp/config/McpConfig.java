package com.mindx360.mcp.config;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * Central Spring configuration for the MCP server.
 *
 * <p>Declares infrastructure beans that are shared across multiple application
 * layers.  Keep this class focused; domain-specific configuration should live
 * in the relevant layer.
 */
@Configuration
public class McpConfig {

    /**
     * Provides a fully configured {@link ObjectMapper} as a Spring bean.
     *
     * <p>Configuration choices:
     * <ul>
     *   <li>{@link JavaTimeModule} – required to serialise {@code LocalDate}
     *       (and other {@code java.time} types) as ISO-8601 strings rather than
     *       numeric timestamps.</li>
     *   <li>{@link SerializationFeature#WRITE_DATES_AS_TIMESTAMPS} disabled –
     *       ensures dates are human-readable in the MCP output Claude sees.</li>
     * </ul>
     *
     * <p>This bean is injected into {@code McpRunner}, {@code McpRequestHandler},
     * and anywhere else Jackson is needed, keeping configuration centralised.
     *
     * @return a singleton {@link ObjectMapper} instance
     */
    @Bean
    public ObjectMapper objectMapper() {
        final ObjectMapper mapper = new ObjectMapper();
        mapper.registerModule(new JavaTimeModule());
        mapper.disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);
        return mapper;
    }
}
