package com.govshield.util;

import com.govshield.model.Enrollment;
import java.util.List;

public class RiskCalculator {

    public static int calculateFraudRiskScore(Enrollment enrollment, List<Enrollment> citizenEnrollments) {
        int riskScore = 0;

        String schemeSector = enrollment.getScheme() != null ? enrollment.getScheme().getSector() : null;

        // Risk factor 1: Multiple enrollments in similar sectors
        long sectorCount = citizenEnrollments.stream()
            .filter(e -> e.getScheme() != null && e.getScheme().getSector() != null)
            .filter(e -> schemeSector != null && e.getScheme().getSector().equalsIgnoreCase(schemeSector))
            .count();
        if (sectorCount > 0) {
            riskScore += 30;
        }

        // Risk factor 2: Rapid successive applications
        long recentApplications = citizenEnrollments.stream()
            .filter(e -> e.getApplicationDate() != null && enrollment.getApplicationDate() != null)
            .filter(e -> java.time.temporal.ChronoUnit.DAYS.between(
                e.getApplicationDate(),
                enrollment.getApplicationDate()) < 30)
            .count();
        if (recentApplications >= 2) {
            riskScore += 25;
        }

        // Risk factor 3: Conflict rule engine flagged this application
        if (Boolean.TRUE.equals(enrollment.getConflictFlag())) {
            riskScore += 20;
        }

        // Risk factor 4: Inconsistent / flagged eligibility
        if ("FLAGGED".equalsIgnoreCase(enrollment.getEligibilityStatus())) {
            riskScore += 15;
        }

        if (riskScore > 100) riskScore = 100;
        if (riskScore < 0) riskScore = 0;
        return riskScore;
    }

    public static String calculateFraudRiskLevelFromScore(int riskScore) {
        if (riskScore >= 61) return "HIGH";
        if (riskScore >= 31) return "MEDIUM";
        return "LOW";
    }

    public static String calculateFraudRisk(Enrollment enrollment, List<Enrollment> citizenEnrollments) {
        return calculateFraudRiskLevelFromScore(calculateFraudRiskScore(enrollment, citizenEnrollments));
    }

    public static boolean shouldFlagForAudit(String riskLevel, List<Enrollment> citizenEnrollments) {
        return "HIGH".equals(riskLevel) || citizenEnrollments.size() > 3;
    }
}
