package com.itcinnovation.backend.user;

public enum UserRole {
    SUPER_ADMIN,
    MANAGER,
    EMPLOYEE;

    public boolean canManageTeam() {
        return this == SUPER_ADMIN || this == MANAGER;
    }
}
