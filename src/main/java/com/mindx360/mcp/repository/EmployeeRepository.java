package com.mindx360.mcp.repository;

import com.mindx360.mcp.entity.Employee;
import com.mindx360.mcp.entity.EmployeeStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * Spring Data JPA repository for {@link Employee}.
 *
 * <p>Follows the repository pattern: callers depend on this interface; the
 * concrete implementation is generated at runtime by Spring Data.
 */
@Repository
public interface EmployeeRepository extends JpaRepository<Employee, Long> {

    // -----------------------------------------------------------------------
    // Department & salary filters (pre-existing)
    // -----------------------------------------------------------------------

    List<Employee> findByDepartment(String department);

    List<Employee> findBySalaryGreaterThan(Double salary);

    List<Employee> findByDepartmentAndSalaryGreaterThan(String department, Double salary);

    // -----------------------------------------------------------------------
    // Status filters
    // -----------------------------------------------------------------------

    /**
     * Returns all employees with the given employment status.
     *
     * @param status {@link EmployeeStatus#ACTIVE} or {@link EmployeeStatus#INACTIVE}
     * @return list of matching employees
     */
    List<Employee> findByStatus(EmployeeStatus status);

    /**
     * Returns employees with the given status inside the given department.
     *
     * @param status     employment status filter
     * @param department department name filter
     * @return list of matching employees
     */
    List<Employee> findByStatusAndDepartment(EmployeeStatus status, String department);

    // -----------------------------------------------------------------------
    // Time-scoped active queries
    // -----------------------------------------------------------------------

    /**
     * Returns ACTIVE employees whose {@code lastActiveDate} falls within the
     * specified calendar month and year.
     *
     * <p>Uses a JPQL query because Spring Data's derived-query DSL does not
     * support YEAR()/MONTH() functions natively.
     *
     * @param year  the four-digit calendar year  (e.g. 2026)
     * @param month the month number 1-12         (e.g. 2 for February)
     * @return list of matching employees
     */
    @Query("SELECT e FROM Employee e "
         + "WHERE e.status = 'ACTIVE' "
         + "AND YEAR(e.lastActiveDate) = :year "
         + "AND MONTH(e.lastActiveDate) = :month")
    List<Employee> findActiveByYearAndMonth(@Param("year") int year,
                                            @Param("month") int month);

    /**
     * Returns employees of any status whose {@code lastActiveDate} falls
     * within the specified calendar month and year.
     *
     * @param year  four-digit year
     * @param month month 1-12
     * @return list of matching employees
     */
    @Query("SELECT e FROM Employee e "
         + "WHERE YEAR(e.lastActiveDate) = :year "
         + "AND MONTH(e.lastActiveDate) = :month")
    List<Employee> findByYearAndMonth(@Param("year") int year,
                                      @Param("month") int month);
}
