package com.govshield.config;

import com.govshield.model.GovEmployee;
import com.govshield.model.Scheme;
import com.govshield.model.SchemeConflictRule;
import com.govshield.repository.GovEmployeeRepository;
import com.govshield.repository.SchemeConflictRuleRepository;
import com.govshield.repository.SchemeRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Arrays;

@Component
public class DataInitializer implements CommandLineRunner {

    private static final Logger logger = LoggerFactory.getLogger(DataInitializer.class);

    @Autowired
    private GovEmployeeRepository govEmployeeRepository;

    @Autowired
    private SchemeRepository schemeRepository;

    @Autowired
    private SchemeConflictRuleRepository schemeConflictRuleRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Override
    public void run(String... args) {
        seedEmployees();
        seedSchemes();
        seedConflictRules();
    }

    private void seedEmployees() {
        if (govEmployeeRepository.count() > 0) {
            return;
        }
        logger.info("Seeding initial government employees...");
        LocalDateTime now = LocalDateTime.now();

        GovEmployee admin = new GovEmployee(
                null, "EMP001", "admin@govshield.gov.in", "Rajesh", "Kumar",
                "9876543210", "Administration", "System Administrator", "ADMIN",
                passwordEncoder.encode("admin@2727"), true, now, now
        );

        GovEmployee officer1 = new GovEmployee(
                null, "EMP002", "officer1@govshield.gov.in", "Priya", "Sharma",
                "9876543211", "Welfare Department", "Scheme Officer", "OFFICER",
                passwordEncoder.encode("officer@2727"), true, now, now
        );

        GovEmployee officer2 = new GovEmployee(
                null, "EMP003", "officer2@govshield.gov.in", "Amit", "Patel",
                "9876543212", "Welfare Department", "Senior Officer", "OFFICER",
                passwordEncoder.encode("officer@2727"), true, now, now
        );

        GovEmployee auditor = new GovEmployee(
                null, "EMP004", "auditor@govshield.gov.in", "Neha", "Gupta",
                "9876543213", "Internal Audit", "Audit Officer", "AUDITOR",
                passwordEncoder.encode("auditor@2727"), true, now, now
        );

        govEmployeeRepository.saveAll(Arrays.asList(admin, officer1, officer2, auditor));
        logger.info("Successfully seeded 4 government employees.");
    }

    private void seedSchemes() {
        if (schemeRepository.count() > 0) {
            return;
        }
        logger.info("Seeding initial schemes...");
        LocalDateTime now = LocalDateTime.now();

        Scheme s1 = new Scheme(
                null, "PM-JAY-001", "Pradhan Mantri Jan Arogya Yojana",
                "Health insurance scheme for vulnerable families", "HEALTH", "FINANCIAL",
                500000.0, 500000.0, 80, 0, false, false, 0, 100, true,
                LocalDate.of(2018, 9, 23), now, now
        );

        Scheme s2 = new Scheme(
                null, "NREGA-001", "Mahatma Gandhi National Rural Employment Guarantee Act",
                "Employment guarantee scheme for rural areas", "EMPLOYMENT", "EMPLOYMENT",
                250000.0, 1000000.0, 70, 18, false, false, 0, 100, true,
                LocalDate.of(2005, 2, 2), now, now
        );

        Scheme s3 = new Scheme(
                null, "PMAY-001", "Pradhan Mantri Awas Yojana",
                "Housing scheme for economically weaker sections", "HOUSING", "HOUSING",
                800000.0, 600000.0, 75, 18, false, false, 0, 100, true,
                LocalDate.of(2015, 6, 25), now, now
        );

        Scheme s4 = new Scheme(
                null, "MGNREGS-001", "Skill Development and Livelihood Scheme",
                "Vocational training and livelihood support", "SKILL_DEVELOPMENT", "TRAINING",
                100000.0, 300000.0, 60, 18, false, false, 0, 100, true,
                LocalDate.of(2019, 5, 1), now, now
        );

        Scheme s5 = new Scheme(
                null, "PM-KISAN-001", "Pradhan Mantri Kisan Samman Nidhi",
                "Direct income support for farmers", "AGRICULTURE", "FINANCIAL",
                60000.0, 2000000.0, 65, 18, true, false, 0, 100, true,
                LocalDate.of(2019, 1, 1), now, now
        );

        schemeRepository.saveAll(Arrays.asList(s1, s2, s3, s4, s5));
        logger.info("Successfully seeded 5 schemes.");
    }

    private void seedConflictRules() {
        if (schemeConflictRuleRepository.count() > 0) {
            return;
        }
        logger.info("Seeding initial scheme conflict rules...");
        LocalDateTime now = LocalDateTime.now();

        SchemeConflictRule r1 = new SchemeConflictRule(
                null, "HOUSING", "HOUSING", "REJECT",
                "Citizen already has an active/previous housing benefit. Only one housing scheme is allowed.",
                true, now, now
        );

        SchemeConflictRule r2 = new SchemeConflictRule(
                null, "AGRICULTURE", "AGRICULTURE", "FLAG",
                "Multiple agriculture scheme applications detected. Requires officer review.",
                true, now, now
        );

        SchemeConflictRule r3 = new SchemeConflictRule(
                null, "EDUCATION", "EDUCATION", "FLAG",
                "Multiple education scheme applications detected. Requires officer review.",
                true, now, now
        );

        schemeConflictRuleRepository.saveAll(Arrays.asList(r1, r2, r3));
        logger.info("Successfully seeded 3 scheme conflict rules.");
    }
}
