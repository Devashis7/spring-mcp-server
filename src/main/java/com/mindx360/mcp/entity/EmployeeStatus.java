package com.mindx360.mcp.entity;

/**
 * Represents the employment status of an {@link Employee}.
 *
 * <p>Stored as a VARCHAR in the H2 database via
 * {@code @Enumerated(EnumType.STRING)} so the value is human-readable in the
 * H2 console and remains stable if ordinal positions ever change.
 *
 * <ul>
 *   <li>{@link #ACTIVE}   – currently employed and active</li>
 *   <li>{@link #INACTIVE} – on leave, terminated, or otherwise inactive</li>
 * </ul>
 */
public enum EmployeeStatus {
    ACTIVE,
    INACTIVE
}
