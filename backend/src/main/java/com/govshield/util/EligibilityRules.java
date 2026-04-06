package com.govshield.util;

import com.govshield.model.Citizen;
import com.govshield.model.Enrollment;
import com.govshield.model.Scheme;

public class EligibilityRules {

    /**
     * Check if a citizen is eligible for a scheme
     */
    public static boolean isEligible(Citizen citizen, Scheme scheme) {
        // Rule 1: Citizen must not be a government employee (if scheme doesn't allow)
        if (!scheme.getIsGovernmentEmployeeEligible() && citizen.getIsGovernmentEmployee()) {
            return false;
        }

        // Rule 2: Annual income check
        if (citizen.getAnnualIncome() > scheme.getMaxAnnualIncome()) {
            return false;
        }

        // Rule 3: Age check
        int age = calculateAge(citizen);
        if (age > scheme.getMaxAge() || age < scheme.getMinAge()) {
            return false;
        }

        return true;
    }

    /**
     * Check for duplicate enrollment from same sector
     */
    public static boolean hasDuplicateEnrollmentInSector(Citizen citizen, Scheme scheme, 
                                                         java.util.List<Enrollment> enrollments) {
        for (Enrollment enrollment : enrollments) {
            if (isSameSector(enrollment.getScheme().getSector(), scheme.getSector()) &&
                !enrollment.getStatus().equals("REJECTED") &&
                !enrollment.getStatus().equals("AUDITOR_REJECTED") &&
                !enrollment.getStatus().equals("OFFICER_REJECTED")) {
                return true;
            }
        }
        return false;
    }

    /**
     * Strict sector rule for citizens:
     * first application in a sector is allowed, any next application in same sector is blocked.
     */
    public static boolean hasAnyApplicationInSector(Scheme scheme, java.util.List<Enrollment> enrollments) {
        return enrollments.stream()
            .anyMatch(enrollment -> isSameSector(enrollment.getScheme().getSector(), scheme.getSector()));
    }

    /**
     * Block repeated attempts in the same sector after multiple submissions.
     */
    public static boolean hasTooManyApplicationsInSector(Scheme scheme, java.util.List<Enrollment> enrollments) {
        long count = enrollments.stream()
            .filter(enrollment -> isSameSector(enrollment.getScheme().getSector(), scheme.getSector()))
            .filter(enrollment ->
                !"REJECTED".equals(enrollment.getStatus()) &&
                !"AUDITOR_REJECTED".equals(enrollment.getStatus()) &&
                !"OFFICER_REJECTED".equals(enrollment.getStatus()))
            .count();
        return count >= 2;
    }

    /**
     * Citizen can receive benefit from only one approved scheme in a sector.
     */
    public static boolean hasReceivedBenefitInSector(Scheme scheme, java.util.List<Enrollment> enrollments) {
        return enrollments.stream()
            .anyMatch(enrollment ->
                isSameSector(enrollment.getScheme().getSector(), scheme.getSector()) &&
                "APPROVED".equalsIgnoreCase(enrollment.getStatus())
            );
    }

    /**
     * Calculate age from date of birth
     */
    private static int calculateAge(Citizen citizen) {
        java.time.LocalDate today = java.time.LocalDate.now();
        return today.getYear() - citizen.getDateOfBirth().getYear();
    }

    /**
     * Check if citizen has income violation
     */
    public static boolean hasIncomeViolation(Citizen citizen, Scheme scheme) {
        return citizen.getAnnualIncome() > scheme.getMaxAnnualIncome();
    }

    /**
     * Check if citizen has employment violation
     */
    public static boolean hasEmploymentViolation(Citizen citizen, Scheme scheme) {
        return !scheme.getIsGovernmentEmployeeEligible() && citizen.getIsGovernmentEmployee();
    }

    private static boolean isSameSector(String firstSector, String secondSector) {
        String first = firstSector == null ? "" : firstSector.trim();
        String second = secondSector == null ? "" : secondSector.trim();
        return first.equalsIgnoreCase(second);
    }
}
