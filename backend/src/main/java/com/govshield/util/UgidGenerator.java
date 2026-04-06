package com.govshield.util;

import java.util.UUID;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;

public class UgidGenerator {

    public static String generateUGID() {
        // Format: UGID-XXXXXXXX-XXXX-XXXX-XXXX-XXXXXXXXXXXX (timestamp-based with UUID)
        String timestamp = String.valueOf(System.currentTimeMillis());
        String uuid = UUID.randomUUID().toString();
        return "UGID-" + timestamp + "-" + uuid;
    }

    public static String generateFromAadhaar(String aadhaar) {
        if (aadhaar == null || aadhaar.length() != 12) {
            throw new IllegalArgumentException("Aadhaar must be 12 digits");
        }
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            byte[] hash = digest.digest(aadhaar.getBytes(StandardCharsets.UTF_8));
            StringBuilder hex = new StringBuilder();
            for (int i = 0; i < 6; i++) {
                hex.append(String.format("%02x", hash[i]));
            }
            return "UGID-" + aadhaar.substring(0, 4) + "-" + hex.toString().toUpperCase();
        } catch (Exception e) {
            throw new RuntimeException("Unable to generate UGID", e);
        }
    }
}
