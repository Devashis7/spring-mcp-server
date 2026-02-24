# Spring Boot MCP Server with Spring AI + Claude Desktop

## Overview

This project demonstrates how to build a **Model Context Protocol (MCP) server** fully inside Spring Boot using **Spring AI**, enabling Claude Desktop to interact with backend business logic through AI tool calling.

Users can ask questions in natural language, and Claude Desktop automatically selects and invokes backend tools exposed via the MCP server.

Example:

User prompt:

> Show employees in IT department with salary greater than 50000

System flow:

1. Claude Desktop interprets the prompt.
2. Claude detects available MCP tools exposed by Spring Boot.
3. Claude calls the appropriate tool.
4. Spring Boot executes business logic.
5. Data fetched from H2 database.
6. Result returned to Claude.

---

# Architecture

## High-Level Flow

```
Claude Desktop (MCP Client)
        ↓
Spring AI MCP Server (Spring Boot)
        ↓
AI Tool Layer (@Tool)
        ↓
Service Layer
        ↓
Spring Data JPA
        ↓
H2 Database
```

---

# What is MCP?

Model Context Protocol (MCP) allows AI systems to interact with external tools in a structured way.

Instead of free-form text responses, AI can:

* Discover available tools
* Understand tool parameters
* Invoke backend methods directly

This creates a structured, reliable integration between AI and backend services.

---

# Role of Spring AI

Spring AI simplifies MCP server implementation by:

* Automatically exposing annotated tools
* Generating JSON schema for tools
* Managing MCP communication
* Handling STDIO transport
* Mapping tool requests to method execution

Without Spring AI, developers would need to:

* Implement JSON-RPC manually
* Manage STDIO streams
* Define tool schemas manually

Spring AI removes this complexity.

---

# How Tool Discovery Works

## Step 1 — Tool Declaration

Example:

```java
@Service
public class EmployeeTools {

    @Tool(description = "Fetch employees by department")
    public List<Employee> getEmployeesByDepartment(String department) {
        return repository.findByDepartment(department);
    }
}
```

Spring AI scans for @Tool annotations during startup.

---

## Step 2 — Tool Schema Generation

Spring AI automatically generates tool metadata:

* Tool name
* Description
* Parameter schema
* Expected response structure

Claude Desktop reads this metadata.

---

## Step 3 — Claude Decision Process

When user enters:

> "List IT employees earning above 60000"

Claude:

1. Evaluates available tools.
2. Matches intent to tool description.
3. Creates structured MCP tool call.

Example conceptual request:

```
{
  "tool": "getEmployeesByDepartment",
  "arguments": {
    "department": "IT"
  }
}
```

---

## Step 4 — Tool Execution

Spring AI:

* Receives MCP request.
* Maps request → method call.
* Executes service logic.
* Returns structured result.

---

# Project Structure

```
mcp/
    MCP server configuration

tool/
    AI-exposed tool classes

service/
    Business logic

repository/
    JPA repositories

entity/
    Database entities

config/
    Application configuration
```

---

# Database Setup

This project uses H2 in-memory database for development.

## Employee Entity

* id
* name
* department
* salary
* joiningDate

---

## H2 Configuration

```
spring.datasource.url=jdbc:h2:mem:testdb
spring.jpa.hibernate.ddl-auto=update
spring.h2.console.enabled=true
```

Access console:

http://localhost:8080/h2-console

---

# Claude Desktop Integration

Claude Desktop acts as MCP client.

Configuration example:

```
{
  "mcpServers": {
    "spring-mcp": {
      "command": "java",
      "args": ["-jar", "target/app.jar"]
    }
  }
}
```

Claude automatically:

* Starts server
* Discovers tools
* Enables AI tool invocation.

---

# FREE vs PAID Components

## FREE

* Spring Boot
* Spring AI MCP Server
* Claude Desktop (local usage)
* H2 database
* Local MCP execution

You can build and test completely free locally.

---

## PAID (OPTIONAL)

Only required if you use external AI APIs instead of Claude Desktop:

| Service    | Paid           |
| ---------- | -------------- |
| Claude API | Yes            |
| OpenAI API | Yes            |
| Gemini API | Partially free |

This project uses Claude Desktop locally, so API costs are avoided.

---

# MCP Lifecycle Diagram

```
User Prompt
     ↓
Claude Reasoning
     ↓
Tool Selection
     ↓
MCP Tool Call
     ↓
Spring AI Execution
     ↓
Database Query
     ↓
Response Returned
```

---

# Best Practices

* Expose domain-specific tools.
* Avoid generic SQL execution tools.
* Provide clear tool descriptions.
* Keep AI logic separate from business logic.

---

# Common Mistakes

❌ Writing manual MCP protocol code (Spring AI handles it).

❌ Exposing raw database access.

❌ Overloading single tool with too many responsibilities.

❌ Using vague tool descriptions.

---

