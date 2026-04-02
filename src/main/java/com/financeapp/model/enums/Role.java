package com.financeapp.model.enums;

/**
 * Defines the roles available in the system.
 * Each role determines what APIs a user can access (RBAC).
 */
public enum Role {
    /** Read-only access: can view dashboard data */
    VIEWER,

    /** Can view financial records and analytics */
    ANALYST,

    /** Full access: manage users and financial records */
    ADMIN
}
