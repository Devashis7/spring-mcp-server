package com.mindx360.mcp;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/**
 * Spring Boot MCP Server – entry point.
 *
 * <p>When launched by Claude Desktop the process communicates exclusively over
 * STDIO using the JSON-RPC 2.0 framing required by the Model Context Protocol.
 * The embedded Tomcat server is kept running on a random port so that the H2
 * console remains reachable during development; it does NOT participate in the
 * MCP communication.
 *
 * <p>Logging is redirected to a rolling file so that stdout stays clean for
 * MCP messages (see logback-spring.xml / application.properties).
 */
@SpringBootApplication
public class SpringMcpApplication {

    public static void main(String[] args) {
        SpringApplication.run(SpringMcpApplication.class, args);
    }
}
