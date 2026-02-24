Perfect 👍 — I’ll write this like a **real professional README.md** that you can directly put into your project.

It will include:

✅ Architecture explanation
✅ Setup steps
✅ MCP + Claude Desktop flow
✅ Spring Boot + Spring AI configuration
✅ H2 dummy DB
✅ What is FREE vs PAID (very clearly explained)
✅ How prompting works
✅ How tools are discovered
✅ Local development workflow

You can copy-paste directly.

---

# 🚀 README.md

```md
# Spring Boot MCP Server with Claude Desktop (Spring AI)

## Overview

This project demonstrates how to build a **Model Context Protocol (MCP) server** using:

- Spring Boot (Java 21)
- Spring AI MCP Server
- Claude Desktop as MCP client
- H2 in-memory database (dummy employee DB)

The goal is to allow users to write **natural language prompts** in Claude Desktop, which automatically invoke backend tools exposed by the Spring Boot application.

Example:

User types in Claude Desktop:

> Show employees in IT department with salary greater than 50000

Claude:

1. Understands user intent.
2. Detects available MCP tools.
3. Calls the appropriate tool.
4. Spring Boot queries database.
5. Result returned to Claude.

---

## Architecture

```

Claude Desktop (MCP Client)
↓
Spring AI MCP Server (Spring Boot)
↓
Tool Layer (@Tool / MCP tool annotations)
↓
Service Layer
↓
Spring Data JPA
↓
H2 In-Memory Database

```

---

## Key Concepts

### MCP (Model Context Protocol)

MCP allows AI clients like Claude Desktop to:

- Discover backend tools
- Understand tool parameters
- Invoke backend logic programmatically

Spring AI MCP server handles:

- Tool registration
- JSON schema generation
- STDIO communication
- Tool execution mapping

No manual protocol handling required.

---

## Technology Stack

| Technology | Purpose |
|---|---|
| Java 21 | Runtime |
| Spring Boot 3.2+ | Backend framework |
| Spring AI MCP Server | MCP integration |
| Claude Desktop | AI client |
| H2 Database | Dummy in-memory DB |
| Spring Data JPA | Data access |

---

## FREE vs PAID Components

### ✅ FREE

- Claude Desktop (local usage)
- Spring Boot
- Spring AI MCP Server
- H2 Database
- Java 21
- Local MCP tool execution

You can build and test the full system locally without paying.

---

### 💰 PAID (OPTIONAL)

If you choose to use external AI APIs instead of Claude Desktop:

| Service | Paid? |
|---|---|
| Claude API (Anthropic API) | Paid |
| OpenAI API | Paid |
| Gemini API (beyond free tier) | Paid |

IMPORTANT:

Claude Desktop itself is free for local MCP integration.

---

## Project Structure

```

src/main/java/com/example/

mcp/
MCP configuration
tool/
Tool classes exposed to Claude
service/
Business logic
repository/
JPA repositories
entity/
Employee entity
config/
Application configuration

```

---

## Employee Database Schema

Entity:

- id (Long)
- name (String)
- department (String)
- salary (Double)
- joiningDate (LocalDate)

H2 is used in-memory for quick development.

---

## H2 Configuration

application.properties:

```

spring.datasource.url=jdbc:h2:mem:testdb
spring.datasource.driverClassName=org.h2.Driver
spring.jpa.database-platform=org.hibernate.dialect.H2Dialect
spring.jpa.hibernate.ddl-auto=update

spring.h2.console.enabled=true
spring.h2.console.path=/h2-console

```

---

## Example Tool

```

@Service
public class EmployeeTools {

@Tool(description="Get employees by department")
public List<Employee> getEmployeesByDepartment(String department) {
return repository.findByDepartment(department);
}

}

```

Spring AI automatically:

- Registers tool
- Creates JSON schema
- Makes tool available to MCP client.

---

## Claude Desktop MCP Configuration

Example configuration:

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

Claude Desktop launches the MCP server process automatically.

---

## Running the Project

### Step 1 — Build

```

mvn clean package

```

### Step 2 — Start via Claude Desktop

Claude Desktop will start the server automatically via MCP config.

---

## Example Usage

User prompt:

> List employees with salary above 60000

Claude:

- Reads tool descriptions.
- Selects appropriate tool.
- Sends MCP request.
- Spring Boot executes tool.
- Returns results.

---

## Why Use MCP Instead of API Calls?

Advantages:

- AI-native architecture
- Automatic tool discovery
- Structured arguments
- Safer than free-form SQL generation
- Clear separation between AI reasoning and backend execution

---

## Development Tips

- Avoid exposing raw SQL tools.
- Create domain-specific tools.
- Provide clear descriptions for tools.
- Keep business logic inside service layer.

---

## Future Enhancements

- Replace H2 with MySQL/Postgres
- Add authentication
- Add multiple tools
- Add vector search
- Deploy as production MCP server

---

## Summary

This project shows how to:

- Build MCP server using Spring Boot.
- Integrate Claude Desktop locally.
- Execute backend logic via AI tool calling.
- Keep architecture clean and scalable.

```

---


