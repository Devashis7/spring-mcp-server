package com.mindx360.mcp.tool;

import com.mindx360.mcp.entity.Employee;
import com.mindx360.mcp.service.EmployeeService;
import com.mindx360.mcp.tool.annotation.Tool;
import com.mindx360.mcp.tool.annotation.ToolParam;
import org.springframework.stereotype.Component;

import java.util.List;

/**
 * Exposes domain-specific MCP tools related to the Employee domain.
 *
 * <p>Implements {@link McpToolProvider} so that {@link com.mindx360.mcp.mcp.McpToolRegistry}
 * discovers this bean via Spring's standard collection-injection without needing
 * to scan the full application context (which would cause a circular dependency).
 *
 * <p>Each public method annotated with {@link Tool} becomes a callable tool
 * in the MCP protocol.
 */
@Component
public class EmployeeTool implements McpToolProvider {

    private final EmployeeService employeeService;

    public EmployeeTool(final EmployeeService employeeService) {
        this.employeeService = employeeService;
    }

    // -----------------------------------------------------------------------
    // Tool: getAllEmployees
    // -----------------------------------------------------------------------

    /**
     * Returns the complete employee roster.
     *
     * <p>Use when the user asks for "all employees", "list everyone",
     * "show the full team", etc.
     *
     * @return list of every employee record
     */
    @Tool(
        name        = "getAllEmployees",
        description = "Retrieves the full list of all employees in the company. "
                    + "Use this when the user wants to see all staff without any filter."
    )
    public List<Employee> getAllEmployees() {
        return employeeService.getAllEmployees();
    }

    // -----------------------------------------------------------------------
    // Tool: getEmployeesByDepartment
    // -----------------------------------------------------------------------

    /**
     * Returns employees filtered by department name.
     *
     * <p>Use when the user specifies a department such as "IT", "HR",
     * "Finance", "Marketing", "Operations", or "Sales".
     *
     * @param department the exact department name (case-sensitive)
     * @return list of employees in that department
     */
    @Tool(
        name        = "getEmployeesByDepartment",
        description = "Retrieves all employees who belong to a specific department. "
                    + "Pass the department name exactly as it appears (e.g. 'IT', 'HR', 'Finance')."
    )
    public List<Employee> getEmployeesByDepartment(
            @ToolParam(
                name        = "department",
                description = "The name of the department to filter by, e.g. 'IT', 'HR', 'Finance'.",
                type        = "string",
                required    = true
            ) String department) {
        return employeeService.getEmployeesByDepartment(department);
    }

    // -----------------------------------------------------------------------
    // Tool: getEmployeesWithSalaryGreaterThan
    // -----------------------------------------------------------------------

    /**
     * Returns employees whose salary exceeds the specified threshold.
     *
     * <p>Use when the user mentions salary comparisons such as "earns more
     * than 60 000", "salary above 75 000", etc.
     *
     * @param amount the salary lower bound (exclusive)
     * @return list of employees earning more than {@code amount}
     */
    @Tool(
        name        = "getEmployeesWithSalaryGreaterThan",
        description = "Retrieves all employees whose annual salary is strictly greater than the "
                    + "given amount. Pass the salary as a numeric value without currency symbols."
    )
    public List<Employee> getEmployeesWithSalaryGreaterThan(
            @ToolParam(
                name        = "amount",
                description = "The minimum salary threshold (exclusive). For example, 50000 means "
                            + "return employees earning more than 50 000.",
                type        = "number",
                required    = true
            ) Double amount) {
        return employeeService.getEmployeesWithSalaryGreaterThan(amount);
    }

    // -----------------------------------------------------------------------
    // Tool: getEmployeesByDepartmentAndSalary  (compound filter)
    // -----------------------------------------------------------------------

    /**
     * Returns employees in a department who also exceed a salary threshold.
     *
     * <p>Use when the user combines a department filter with a salary
     * condition, e.g. "IT employees earning more than 50 000".
     *
     * @param department department name
     * @param amount     minimum salary (exclusive)
     * @return filtered employee list
     */
    @Tool(
        name        = "getEmployeesByDepartmentAndSalary",
        description = "Retrieves employees who belong to a specific department AND whose salary "
                    + "is greater than the provided amount. Use this for compound queries like "
                    + "'show IT employees with salary above 50000'."
    )
    public List<Employee> getEmployeesByDepartmentAndSalary(
            @ToolParam(
                name        = "department",
                description = "The department name to filter by, e.g. 'IT'.",
                type        = "string",
                required    = true
            ) String department,
            @ToolParam(
                name        = "amount",
                description = "The minimum salary threshold (exclusive), e.g. 50000.",
                type        = "number",
                required    = true
            ) Double amount) {
        return employeeService.getEmployeesByDepartmentAndSalaryGreaterThan(department, amount);
    }

    // -----------------------------------------------------------------------
    // Tool: getEmployeeById
    // -----------------------------------------------------------------------

    /**
     * Looks up a single employee by their unique identifier.
     *
     * @param id the employee's primary key
     * @return list containing the matching employee or an empty list
     */
    @Tool(
        name        = "getEmployeeById",
        description = "Retrieves the details of a single employee identified by their numeric ID. "
                    + "Use this when the user already knows the employee ID."
    )
    public List<Employee> getEmployeeById(
            @ToolParam(
                name        = "id",
                description = "The unique numeric identifier of the employee.",
                type        = "number",
                required    = true
            ) Long id) {
        return employeeService.getEmployeeById(id)
                .map(List::of)
                .orElse(List.of());
    }

    // -----------------------------------------------------------------------
    // Tool: getActiveEmployees
    // -----------------------------------------------------------------------

    /**
     * Returns every employee whose status is ACTIVE.
     *
     * <p>Use when the user asks for "active employees", "who is currently
     * active", "show active staff", etc.
     *
     * @return list of all ACTIVE employees
     */
    @Tool(
        name        = "getActiveEmployees",
        description = "Retrieves all employees whose employment status is ACTIVE. "
                    + "Use this when the user asks for active employees or current staff."
    )
    public List<Employee> getActiveEmployees() {
        return employeeService.getActiveEmployees();
    }

    // -----------------------------------------------------------------------
    // Tool: getInactiveEmployees
    // -----------------------------------------------------------------------

    /**
     * Returns every employee whose status is INACTIVE.
     *
     * <p>Use when the user asks for "inactive employees", "employees on
     * leave", "who is no longer active", etc.
     *
     * @return list of all INACTIVE employees
     */
    @Tool(
        name        = "getInactiveEmployees",
        description = "Retrieves all employees whose employment status is INACTIVE. "
                    + "Use this when the user asks for inactive, on-leave, or former employees."
    )
    public List<Employee> getInactiveEmployees() {
        return employeeService.getInactiveEmployees();
    }

    // -----------------------------------------------------------------------
    // Tool: getEmployeesByStatus
    // -----------------------------------------------------------------------

    /**
     * Returns employees filtered by an explicit status string.
     *
     * <p>Use when the user explicitly mentions "ACTIVE" or "INACTIVE" as a
     * status label alongside another condition.
     *
     * @param status "ACTIVE" or "INACTIVE" (case-insensitive)
     * @return list of matching employees
     */
    @Tool(
        name        = "getEmployeesByStatus",
        description = "Retrieves all employees matching the given status. "
                    + "Pass 'ACTIVE' for currently active employees or 'INACTIVE' for inactive ones."
    )
    public List<Employee> getEmployeesByStatus(
            @ToolParam(
                name        = "status",
                description = "Employment status to filter by. Accepted values: 'ACTIVE' or 'INACTIVE'.",
                type        = "string",
                required    = true
            ) String status) {
        return employeeService.getEmployeesByStatus(status);
    }

    // -----------------------------------------------------------------------
    // Tool: getActiveEmployeesByDepartment
    // -----------------------------------------------------------------------

    /**
     * Returns ACTIVE employees in the specified department.
     *
     * <p>Use for prompts like "show active IT employees" or
     * "who is currently active in Finance".
     *
     * @param department the department name
     * @return list of active employees in that department
     */
    @Tool(
        name        = "getActiveEmployeesByDepartment",
        description = "Retrieves all ACTIVE employees who belong to a specific department. "
                    + "Use this for queries like 'active employees in IT' or 'who is active in HR'."
    )
    public List<Employee> getActiveEmployeesByDepartment(
            @ToolParam(
                name        = "department",
                description = "The department name to filter by, e.g. 'IT', 'HR', 'Finance'.",
                type        = "string",
                required    = true
            ) String department) {
        return employeeService.getActiveEmployeesByDepartment(department);
    }

    // -----------------------------------------------------------------------
    // Tool: getActiveEmployeesThisMonth
    // -----------------------------------------------------------------------

    /**
     * Returns ACTIVE employees whose {@code lastActiveDate} falls in the
     * <em>current</em> calendar month.
     *
     * <p>Use when the user asks about "active users this month",
     * "who was active this month", "active employees in the current month",
     * etc.  The server resolves the current month automatically.
     *
     * @return list of active employees this month
     */
    @Tool(
        name        = "getActiveEmployeesThisMonth",
        description = "Retrieves all ACTIVE employees whose last active date falls within the "
                    + "current calendar month. Use this for prompts like 'active users this month', "
                    + "'who was active this month', or 'show this month active employees'."
    )
    public List<Employee> getActiveEmployeesThisMonth() {
        return employeeService.getActiveEmployeesThisMonth();
    }

    // -----------------------------------------------------------------------
    // Tool: getActiveEmployeesByMonth
    // -----------------------------------------------------------------------

    /**
     * Returns ACTIVE employees whose {@code lastActiveDate} falls in the
     * given month and year.
     *
     * <p>Use when the user specifies a particular month such as
     * "active users in January 2026" or "active employees last month".
     *
     * @param year  four-digit year (e.g. 2026)
     * @param month month number 1-12 (e.g. 1 for January)
     * @return list of active employees for that month
     */
    @Tool(
        name        = "getActiveEmployeesByMonth",
        description = "Retrieves all ACTIVE employees whose last active date falls within a "
                    + "specific month and year. Use this when the user specifies a particular month, "
                    + "e.g. 'active employees in January 2026' or 'active users last month'. "
                    + "Pass year as a 4-digit number and month as 1-12."
    )
    public List<Employee> getActiveEmployeesByMonth(
            @ToolParam(
                name        = "year",
                description = "The 4-digit calendar year, e.g. 2026.",
                type        = "number",
                required    = true
            ) Long year,
            @ToolParam(
                name        = "month",
                description = "The month number (1 = January … 12 = December), e.g. 2 for February.",
                type        = "number",
                required    = true
            ) Long month) {
        return employeeService.getActiveEmployeesByMonth(year.intValue(), month.intValue());
    }
}
