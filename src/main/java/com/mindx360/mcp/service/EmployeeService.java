package com.mindx360.mcp.service;

import com.mindx360.mcp.entity.Employee;
import com.mindx360.mcp.entity.EmployeeStatus;
import com.mindx360.mcp.repository.EmployeeRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

/**
 * Business-logic layer for Employee operations.
 *
 * <p>This service acts as a façade between the tool layer and the persistence
 * layer.  It is the only component allowed to interact with
 * {@link EmployeeRepository}.  Applying {@link Transactional} at the service
 * level keeps transaction boundaries explicit and testable.
 */
@Service
@Transactional(readOnly = true)
public class EmployeeService {

    private final EmployeeRepository employeeRepository;

    /** Constructor injection – preferred over field injection for testability. */
    public EmployeeService(final EmployeeRepository employeeRepository) {
        this.employeeRepository = employeeRepository;
    }

    // -----------------------------------------------------------------------
    // Read operations
    // -----------------------------------------------------------------------

    /**
     * Returns every employee in the database in insertion order.
     *
     * @return unmodifiable view of all employees
     */
    public List<Employee> getAllEmployees() {
        return employeeRepository.findAll();
    }

    /**
     * Returns the employee identified by the given primary key.
     *
     * @param id the unique identifier
     * @return an Optional containing the employee if found, empty otherwise
     */
    public Optional<Employee> getEmployeeById(final Long id) {
        return employeeRepository.findById(id);
    }

    /**
     * Returns all employees that belong to the specified department.
     *
     * @param department the exact department name (case-sensitive)
     * @return list of matching employees; never {@code null}
     */
    public List<Employee> getEmployeesByDepartment(final String department) {
        return employeeRepository.findByDepartment(department);
    }

    /**
     * Returns all employees whose annual salary exceeds {@code amount}.
     *
     * @param amount the salary threshold (exclusive lower bound)
     * @return list of matching employees; never {@code null}
     */
    public List<Employee> getEmployeesWithSalaryGreaterThan(final Double amount) {
        return employeeRepository.findBySalaryGreaterThan(amount);
    }

    /**
     * Returns employees in a specific department whose salary exceeds the
     * specified amount.  Allows a compound filter in a single database trip.
     *
     * @param department department name filter
     * @param amount     minimum salary (exclusive)
     * @return list of matching employees; never {@code null}
     */
    public List<Employee> getEmployeesByDepartmentAndSalaryGreaterThan(
            final String department, final Double amount) {
        return employeeRepository.findByDepartmentAndSalaryGreaterThan(department, amount);
    }

    // -----------------------------------------------------------------------
    // Status-based queries
    // -----------------------------------------------------------------------

    /**
     * Returns all employees with the given status string ("ACTIVE" or
     * "INACTIVE", case-insensitive).
     *
     * @param status the status string
     * @return list of matching employees
     * @throws IllegalArgumentException if {@code status} is not a valid
     *         {@link EmployeeStatus} value
     */
    public List<Employee> getEmployeesByStatus(final String status) {
        final EmployeeStatus employeeStatus = EmployeeStatus.valueOf(status.toUpperCase());
        return employeeRepository.findByStatus(employeeStatus);
    }

    /**
     * Returns all ACTIVE employees.
     *
     * @return list of active employees
     */
    public List<Employee> getActiveEmployees() {
        return employeeRepository.findByStatus(EmployeeStatus.ACTIVE);
    }

    /**
     * Returns all INACTIVE employees.
     *
     * @return list of inactive employees
     */
    public List<Employee> getInactiveEmployees() {
        return employeeRepository.findByStatus(EmployeeStatus.INACTIVE);
    }

    /**
     * Returns ACTIVE employees in the specified department.
     *
     * @param department department name
     * @return list of matching employees
     */
    public List<Employee> getActiveEmployeesByDepartment(final String department) {
        return employeeRepository.findByStatusAndDepartment(EmployeeStatus.ACTIVE, department);
    }

    // -----------------------------------------------------------------------
    // Time-scoped queries
    // -----------------------------------------------------------------------

    /**
     * Returns ACTIVE employees whose {@code lastActiveDate} falls in the
     * <em>current</em> calendar month (relative to the server clock).
     *
     * @return list of active employees this month
     */
    public List<Employee> getActiveEmployeesThisMonth() {
        final LocalDate today = LocalDate.now();
        return employeeRepository.findActiveByYearAndMonth(today.getYear(),
                                                           today.getMonthValue());
    }

    /**
     * Returns ACTIVE employees whose {@code lastActiveDate} falls in the
     * given month and year.
     *
     * @param year  four-digit year
     * @param month month 1-12
     * @return list of matching employees
     */
    public List<Employee> getActiveEmployeesByMonth(final int year, final int month) {
        return employeeRepository.findActiveByYearAndMonth(year, month);
    }
}
