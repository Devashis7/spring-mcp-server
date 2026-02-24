```md
# Spring Boot MCP Server with Claude Desktop (No Node.js)

This project demonstrates how to build a **Model Context Protocol (MCP) server** using **Spring Boot (Java 21)** that integrates directly with **Claude Desktop**.

The goal is to allow users to type natural language queries inside Claude Desktop, which will then automatically call backend tools exposed by the Spring Boot MCP server to fetch and return data from a database.

This implementation:

- Uses **Spring Boot as MCP server**
- Uses **Claude Desktop as MCP client**
- Uses **H2 database** as a dummy Employee database
- Does NOT use Node.js
- Follows tool-based architecture (no raw SQL execution)

---

# 🚀 High-Level Architecture

```

User Prompt (Natural Language)
↓
Claude Desktop (MCP Client)
↓  MCP Protocol (STDIO)
Spring Boot MCP Server
↓
Tool Layer (@Tool methods)
↓
Service Layer
↓
Spring Data JPA
↓
H2 In-Memory Database

```

---

# 🧠 How It Works

1. Claude Desktop starts the Spring Boot MCP server.
2. MCP server exposes available tools with schema and descriptions.
3. User enters a prompt inside Claude Desktop.
4. Claude analyzes available tools and selects one.
5. MCP server executes tool method.
6. Data is fetched from H2 database.
7. Result is returned to Claude Desktop.

---

# 📦 Tech Stack

- Java 21
- Spring Boot 3.2+
- Spring AI MCP Server
- Spring Data JPA
- H2 Database (In-memory)
- Claude Desktop (MCP enabled)

---

# 📁 Project Structure

```

src/main/java/
│
├── config/
├── entity/
│   └── Employee.java
├── repository/
│   └── EmployeeRepository.java
├── service/
│   └── EmployeeService.java
├── tool/
│   └── EmployeeTools.java
├── mcp/
│   ├── McpConfig.java
│   ├── McpRunner.java
│   └── McpRequestHandler.java
└── Application.java

```

---

# 🗄️ Dummy Employee Database

## Employee Entity Fields

- id (Long)
- name (String)
- department (String)
- salary (Double)
- joiningDate (LocalDate)

---

## H2 Configuration

Add to `application.properties`:

```

spring.datasource.url=jdbc:h2:mem:testdb
spring.datasource.driverClassName=org.h2.Driver
spring.datasource.username=sa
spring.datasource.password=

spring.jpa.hibernate.ddl-auto=update
spring.jpa.database-platform=org.hibernate.dialect.H2Dialect

spring.h2.console.enabled=true
spring.h2.console.path=/h2-console

```

Access H2 console:

```

[http://localhost:8080/h2-console](http://localhost:8080/h2-console)

```

---

## data.sql Example

```

INSERT INTO employee(id,name,department,salary,joining_date)
VALUES (1,'Rahul','IT',70000,'2023-01-01');

INSERT INTO employee(id,name,department,salary,joining_date)
VALUES (2,'Priya','HR',50000,'2022-05-10');

````

---

# 🛠️ MCP Tool Implementation

Example Tool Class:

```java
@Service
public class EmployeeTools {

    @Tool(description = "Get employees by department")
    public List<Employee> getEmployeesByDepartment(String department) {
        return employeeRepository.findByDepartment(department);
    }

    @Tool(description = "Get employees with salary greater than given amount")
    public List<Employee> getHighSalaryEmployees(Double salary) {
        return employeeRepository.findBySalaryGreaterThan(salary);
    }
}
````

---

# ⚙️ Claude Desktop MCP Configuration

Add MCP server configuration:

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

Claude Desktop will:

* Start the Spring Boot app.
* Discover tools automatically.
* Use them when prompts match.

---

# 💬 Example Usage

User prompt inside Claude Desktop:

```
Show employees from IT department with salary greater than 60000
```

Claude Desktop will:

1. Analyze available tools.
2. Select appropriate tool.
3. Send MCP tool request.
4. Spring Boot executes method.
5. Results returned to Claude.

---

# ✅ Tool Design Best Practices

Good:

```
getEmployeesByDepartment(String department)
getHighSalaryEmployees(Double threshold)
```

Avoid:

```
executeSql(String query)
```

Reasons:

* Security risks
* Poor AI mapping
* Harder tool discovery

---

# 🔁 Development Workflow

1. Run Spring Boot application.
2. Configure MCP server in Claude Desktop.
3. Restart Claude Desktop.
4. Test prompts.
5. Observe tool invocation automatically.

---

# 📊 Different MCP Approaches (Overview)

## 1. Spring AI Tool Calling (Non-MCP)

Backend calls AI API and executes tools internally.

## 2. Spring AI MCP Server

Backend exposes tools externally via MCP.

## 3. Claude Desktop MCP Tools

Claude Desktop acts as AI orchestrator.

---

# 🧩 Future Enhancements

* Replace H2 with MySQL or PostgreSQL
* Add authentication
* Add advanced filtering tools
* Add vector search
* Deploy remote MCP server

---

# 📚 Summary

This project demonstrates a fully self-contained Spring Boot MCP server:

* No Node.js required
* Claude Desktop controls tool execution
* Tools expose domain logic
* Database queries executed securely

This architecture is ideal for:

* Local AI assistants
* Experimentation
* Tool-driven AI workflows

---

```
::contentReference[oaicite:0]{index=0}
```
