package com.quanlynhatro.config;

public final class AppRoles {
    private AppRoles() {
    }

    // Domain naming for clarity in code. Keep mapped authorities unchanged for compatibility.
    public static final String LANDLORD_AUTHORITY = "ROLE_ADMIN";
    public static final String TENANT_AUTHORITY = "ROLE_USER";
}
