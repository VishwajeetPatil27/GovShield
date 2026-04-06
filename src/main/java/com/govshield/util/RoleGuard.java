package com.govshield.util;

import com.govshield.exception.CustomException;
import java.util.Arrays;

public class RoleGuard {

    public static void ensureRole(String role, String... allowedRoles) {
        if (role == null) {
            throw new CustomException("Missing user role", "MISSING_ROLE", 403);
        }
        boolean allowed = Arrays.stream(allowedRoles).anyMatch(r -> r.equalsIgnoreCase(role));
        if (!allowed) {
            throw new CustomException("Operation not permitted for role: " + role, "FORBIDDEN", 403);
        }
    }
}
