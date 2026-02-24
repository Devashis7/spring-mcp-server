# Architecture Difference: Spring AI MCP Server vs Manual MCP Implementation

## Overview

This document explains the architectural differences between two approaches for building a local MCP (Model Context Protocol) server using Spring Boot and Claude Desktop.

### Approach A — Spring AI MCP Server (Framework-driven)

Uses:

* Spring AI MCP Server starter
* Annotation-based tool exposure (`@Tool`)
* Automatic MCP protocol handling

### Approach B — Manual MCP Implementation

Uses:

* Custom MCP protocol implementation
* Manual STDIO / JSON-RPC handling
* Explicit tool registry and request parsing

---

# High-Level Comparison

| Category               | Spring AI MCP Server         | Manual MCP Implementation    |
| ---------------------- | ---------------------------- | ---------------------------- |
| MCP Protocol Handling  | Automatic                    | Manual                       |
| Tool Discovery         | Auto-scanned via annotations | Custom registry required     |
| JSON Schema Generation | Automatic                    | Must be manually implemented |
| STDIO Communication    | Built-in                     | Developer must implement     |
| Complexity             | Low                          | High                         |
| Control Level          | Moderate                     | Full control                 |
| Development Speed      | Fast                         | Slower                       |
| Error Risk             | Low                          | Higher                       |
| Maintenance Effort     | Lower                        | Higher                       |

---

# Architecture Differences

## Approach A — Spring AI MCP Server

```

Claude Desktop
↓
Spring AI MCP Server
↓
@Tool Annotated Services
↓
Business Logic
↓
Database

```

Spring AI acts as:

* MCP protocol engine
* Tool discovery engine
* JSON schema generator
* Request router

Developers focus primarily on business logic.

---

## Approach B — Manual MCP Server

```

Claude Desktop
↓
Custom STDIO Handler
↓
MCP Request Parser
↓
Manual Tool Registry
↓
Business Logic
↓
Database

```

Developer must implement:

* STDIO reading/writing
* JSON-RPC handling
* Tool schema generation
* Tool invocation mapping

---

# Tool Discovery Differences

## Spring AI MCP

Tools declared using annotations:

```

@Tool(description="Get employees by department")
public List<Employee> getEmployeesByDepartment(String dept)

```

Spring AI:

* Scans application context
* Registers tools automatically
* Generates JSON schema
* Exposes tool metadata to Claude

---

## Manual MCP

Developer must:

* Define tool metadata manually
* Maintain tool registry map
* Serialize parameter schema
* Maintain consistency between tool definition and execution logic

---

# Communication Handling

## Spring AI MCP

Spring AI internally manages:

* STDIO transport
* MCP lifecycle
* Request/response serialization

No direct protocol handling needed.

---

## Manual MCP

Developer must implement:

* Input stream listener
* JSON parsing
* Tool routing logic
* Output serialization

Example responsibilities:

* McpRunner
* McpRequestHandler
* McpResponseBuilder

---

# Role of @Tool Annotation

## Spring AI MCP

`@Tool` is first-class:

* Auto-detected
* Used to generate MCP tool schema
* Integrated into MCP lifecycle

---

## Manual MCP

`@Tool` is optional:

* Only for code organization
* MCP exposure still manual
* No automatic discovery

---

# Development Complexity

## Spring AI MCP

Advantages:

* Minimal boilerplate
* Faster prototyping
* Cleaner architecture
* Less protocol knowledge required

---

## Manual MCP

Advantages:

* Full control over protocol behavior
* Custom transport possible
* Fine-grained lifecycle control

Disadvantages:

* Higher implementation effort
* Greater risk of protocol errors

---

# Extensibility

## Spring AI MCP

Easy to:

* Add new tools
* Modify tool parameters
* Integrate additional services

Limited when:

* Custom protocol behavior required.

---

## Manual MCP

Maximum flexibility:

* Custom message routing
* Advanced protocol extensions
* Custom serialization strategies

---

# Performance Considerations

Spring AI MCP:

* Slight abstraction overhead
* Faster development time.

Manual MCP:

* Potentially more optimized
* Higher engineering cost.

---

# Production Considerations

Spring AI MCP recommended when:

* Standard MCP usage
* Tool-driven architecture
* Rapid development desired.

Manual MCP recommended when:

* Custom protocol logic required
* Non-standard transport needed
* Deep control over communication layer.

---

# Why Spring AI Approach is Preferred

Spring AI provides:

* Protocol abstraction
* Annotation-driven configuration
* Automatic schema generation
* Reduced boilerplate
* Better maintainability

This aligns with enterprise Spring Boot practices.

---

# Summary

Spring AI MCP server transforms MCP integration from:

"Protocol engineering problem"

into:

"Standard Spring service development".

Manual MCP implementation gives more control but requires significantly more engineering effort and protocol knowledge.

For most local Claude Desktop integrations and enterprise Spring Boot projects, Spring AI MCP server approach is strongly recommended.

```
```
