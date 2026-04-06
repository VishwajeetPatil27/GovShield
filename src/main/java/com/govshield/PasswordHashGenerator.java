package com.govshield;

import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;

public class PasswordHashGenerator {
    public static void main(String[] args) {
        BCryptPasswordEncoder encoder = new BCryptPasswordEncoder();
        String password = (args != null && args.length > 0 && args[0] != null && !args[0].isBlank())
                ? args[0]
                : "password123";
        String hash = encoder.encode(password);
        System.out.println("Bcrypt hash for '" + password + "': " + hash);
    }
}
