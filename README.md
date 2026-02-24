# Spring Boot MCP Server

A **fully self-contained** Model Context Protocol (MCP) server implemented in Java 21 / Spring Boot 3.2, integrating with Claude Desktop over **STDIO** transport.  No Node.js bridge required.

---

## Architecture Overview

```
Claude Desktop
    │
    │  JSON-RPC 2.0 over STDIO (stdin / stdout)
    ▼
┌─────────────────────────────────────────────┐
│           Spring Boot MCP Server            │
│                                             │
│  McpRunner  ──►  McpRequestHandler          │
│                       │                     │
│               McpToolRegistry               │
│                       │                     │
│               EmployeeTool  (@Component)    │
│                       │                     │
│               EmployeeService               │
│                       │                     │
│           EmployeeRepository (JPA)          │
│                       │                     │
│          H2 In-Memory Database              │
└─────────────────────────────────────────────┘
```

---

## Package Structure

```
src/main/java/com/mindx360/mcp/
├── SpringMcpApplication.java          ← entry point
│
├── config/
│   └── McpConfig.java                 ← ObjectMapper bean
│
├── entity/
│   └── Employee.java                  ← JPA entity
│
├── repository/
│   └── EmployeeRepository.java        ← Spring Data JPA
│
├── service/
│   └── EmployeeService.java           ← business logic
│
├── tool/
│   ├── annotation/
│   │   ├── Tool.java                  ← @Tool method annotation
│   │   └── ToolParam.java             ← @ToolParam parameter annotation
│   └── EmployeeTool.java              ← domain tools exposed to Claude
│
└── mcp/
    ├── model/
    │   ├── McpRequest.java            ← JSON-RPC 2.0 request envelope
    │   ├── McpResponse.java           ← JSON-RPC 2.0 response envelope
    │   ├── McpToolDefinition.java     ← tools/list schema object
    │   └── McpError.java              ← JSON-RPC error object
    ├── ToolEntry.java                 ← (bean, method, schema) binding
    ├── McpToolRegistry.java           ← auto-discovers @Tool methods
    ├── McpRequestHandler.java         ← dispatches MCP methods
    └── McpRunner.java                 ← STDIO read/write loop
```

---

## Employee Database Schema

| Column            | Type       | Notes                          |
|-------------------|------------|--------------------------------|
| `id`              | BIGINT     | Auto-generated PK              |
| `name`            | VARCHAR    | Full name                      |
| `department`      | VARCHAR    | IT, HR, Finance, …             |
| `salary`          | DOUBLE     | Annual salary (USD)            |
| `joining_date`    | DATE       | ISO-8601                       |
| `status`          | VARCHAR    | `ACTIVE` or `INACTIVE`         |
| `last_active_date`| DATE       | Most recent active date (ISO-8601) |

20 seed rows across 6 departments.  12 employees are ACTIVE; 8 are INACTIVE.
Several ACTIVE employees have a `last_active_date` in February 2026, making
`getActiveEmployeesThisMonth` return meaningful results out-of-the-box.

---

## Available MCP Tools

| Tool name | Parameters | Description |
|-----------|-----------|-------------|
| `getAllEmployees` | – | Returns every employee |
| `getEmployeesByDepartment` | `department: string` | Filters by department |
| `getEmployeesWithSalaryGreaterThan` | `amount: number` | Salary threshold filter |
| `getEmployeesByDepartmentAndSalary` | `department: string`, `amount: number` | Compound filter |
| `getEmployeeById` | `id: number` | Single employee lookup |
| `getActiveEmployees` | – | All ACTIVE employees |
| `getInactiveEmployees` | – | All INACTIVE employees |
| `getEmployeesByStatus` | `status: string` | Filter by "ACTIVE" or "INACTIVE" |
| `getActiveEmployeesByDepartment` | `department: string` | ACTIVE employees in a department |
| `getActiveEmployeesThisMonth` | – | ACTIVE employees whose `lastActiveDate` is in the **current** month |
| `getActiveEmployeesByMonth` | `year: number`, `month: number` | ACTIVE employees for a specific month |

---

## Build

### Prerequisites

| Tool | Version |
|------|---------|
| JDK  | 21+     |
| Maven | 3.9+  |

```bash
# Clone / open the project, then:
cd spring-mcp-server
mvn clean package -DskipTests
```

The fat JAR is built to `target/spring-mcp-server-1.0.0.jar`.

---

## Claude Desktop Integration

### 1. Find your Claude Desktop config file

| OS      | Path |
|---------|------|
| Windows | `%APPDATA%\Claude\claude_desktop_config.json` |
| macOS   | `~/Library/Application Support/Claude/claude_desktop_config.json` |
| Linux   | `~/.config/Claude/claude_desktop_config.json` |

### 2. Add the MCP server entry

```json
{
  "mcpServers": {
    "spring-mcp": {
      "command": "java",
      "args": [
        "-jar",
        "D:\\MindX360\\MCP\\spring-mcp-server\\target\\spring-mcp-server-1.0.0.jar"
      ]
    }
  }
}
```

> **Windows note:** use forward slashes or escaped back-slashes in the path.

### 3. Restart Claude Desktop

Claude will spawn the JAR as a child process connected via STDIO.

---

## How Claude Maps Prompts to Tool Calls

### Example

**User types in Claude Desktop:**
```
Show employees in IT department with salary greater than 50000
```

**Step-by-step flow:**

1. **Claude reads the tools/list** (sent once on connection):
   ```json
   {
     "name": "getEmployeesByDepartmentAndSalary",
     "description": "Retrieves employees who belong to a specific department AND whose salary is greater than the provided amount. Use this for compound queries like 'show IT employees with salary above 50000'.",
     "inputSchema": {
       "type": "object",
       "properties": {
         "department": { "type": "string", "description": "The department name to filter by, e.g. 'IT'." },
         "amount":     { "type": "number", "description": "The minimum salary threshold (exclusive), e.g. 50000." }
       },
       "required": ["department", "amount"]
     }
   }
   ```

2. **Claude decides to call `getEmployeesByDepartmentAndSalary`** and sends:
   ```json
   {
     "jsonrpc": "2.0",
     "id": 3,
     "method": "tools/call",
     "params": {
       "name": "getEmployeesByDepartmentAndSalary",
       "arguments": { "department": "IT", "amount": 50000 }
     }
   }
   ```

3. **McpRunner** reads the line → **McpRequestHandler** dispatches to `tools/call` handler → **McpToolRegistry** invokes `EmployeeTool.getEmployeesByDepartmentAndSalary("IT", 50000.0)` → **EmployeeService** → **H2 database**.

4. **Response** written to stdout:
   ```json
   {
     "jsonrpc": "2.0",
     "id": 3,
     "result": {
       "content": [
         {
           "type": "text",
           "text": "[\n  {\n    \"id\": 1,\n    \"name\": \"Alice Johnson\",\n    \"department\": \"IT\",\n    \"salary\": 95000.0,\n    \"joiningDate\": \"2019-03-15\"\n  },\n  ...\n  ]"
         }
       ],
       "isError": false
     }
   }
   ```

5. **Claude renders the result** in a natural-language reply to the user.

---

## More Example Prompts

```
List all employees
→ getAllEmployees()

Who works in Finance?
→ getEmployeesByDepartment("Finance")

Which employees earn more than 80000?
→ getEmployeesWithSalaryGreaterThan(80000)

Show me the employee with ID 5
→ getEmployeeById(5)

HR employees earning over 60000
→ getEmployeesByDepartmentAndSalary("HR", 60000)

Show all active employees
→ getActiveEmployees()

Show all inactive employees
→ getInactiveEmployees()

Active employees in IT
→ getActiveEmployeesByDepartment("IT")

Active users this month
→ getActiveEmployeesThisMonth()          ← server resolves current month automatically

Active employees in January 2026
→ getActiveEmployeesByMonth(2026, 1)

Who was active last month?
→ getActiveEmployeesByMonth(2026, 1)     ← Claude deduces last month from context
```

---

## H2 Console (Development)

While the server is running (e.g. in a terminal), open:

```
http://localhost:8090/h2-console
```

| Field    | Value                                         |
|----------|-----------------------------------------------|
| JDBC URL | `jdbc:h2:mem:employeedb;DB_CLOSE_DELAY=-1`    |
| Username | `sa`                                          |
| Password | *(blank)*                                     |

> Note: the H2 console is only useful when you **run the server standalone** (not via Claude Desktop, which closes stdin immediately on shutdown).

---

## Extending to MySQL / PostgreSQL

1. Replace the H2 dependency in `pom.xml` with the target JDBC driver.
2. Update `application.properties`:
   ```properties
   spring.datasource.url=jdbc:mysql://localhost:3306/employeedb
   spring.datasource.username=root
   spring.datasource.password=secret
   spring.jpa.database-platform=org.hibernate.dialect.MySQLDialect
   spring.jpa.hibernate.ddl-auto=validate
   ```
3. Provide a proper migration script (Flyway / Liquibase recommended).
4. No changes required to the MCP or tool layers.

---

## MCP Protocol Reference

| Method | Direction | Purpose |
|--------|-----------|---------|
| `initialize` | Client → Server | Handshake, exchange capabilities |
| `notifications/initialized` | Client → Server | Confirm init (no reply) |
| `tools/list` | Client → Server | Enumerate available tools |
| `tools/call` | Client → Server | Invoke a specific tool |

All messages are newline-delimited UTF-8 JSON (JSON-RPC 2.0).
