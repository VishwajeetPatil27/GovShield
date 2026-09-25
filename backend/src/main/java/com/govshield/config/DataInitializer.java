package com.govshield.config;

import com.govshield.model.*;
import com.govshield.repository.*;
import com.govshield.util.UgidGenerator;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.*;

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
    private CitizenRepository citizenRepository;

    @Autowired
    private ProjectRepository projectRepository;

    @Autowired
    private EnrollmentRepository enrollmentRepository;

    @Autowired
    private AuditLogRepository auditLogRepository;

    @Autowired
    private CitizenDocumentRepository citizenDocumentRepository;

    @Autowired
    private CitizenEconomicProfileRepository citizenEconomicProfileRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Override
    public void run(String... args) {
        seedEmployees();
        seedSchemes();
        seedConflictRules();
        seedCitizens();
        seedProjects();
        seedEnrollments();
        seedAuditLogs();
        seedCitizenDocuments();
        seedCepsProfiles();
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

    private void seedCitizens() {
        LocalDate today = LocalDate.now();

        createCitizenIfAbsent(
                "123456789012", "ABCDE1234F", "Ramesh", "Patel",
                "ramesh.patel@email.com", "9876543210", "9988776655001",
                LocalDate.of(1985, 5, 15), "MALE", "123 Main Street",
                "Gujarat", "Ahmedabad", "380001", 250000.0, "UNEMPLOYED",
                false, true, "VERIFIED", today
        );

        createCitizenIfAbsent(
                "234567890123", "BCDEF2345G", "Priya", "Singh",
                "priya.singh@email.com", "9876543211", "9988776655001",
                LocalDate.of(1990, 8, 22), "FEMALE", "456 Oak Avenue",
                "Maharashtra", "Mumbai", "400001", 350000.0, "SELF_EMPLOYED",
                false, false, "UNDER_REVIEW", today
        );

        createCitizenIfAbsent(
                "345678901234", "CDEFG3456H", "Suresh", "Verma",
                "suresh.verma@email.com", "9876543212", "9988776655002",
                LocalDate.of(1978, 3, 10), "MALE", "789 Pine Road",
                "Rajasthan", "Jaipur", "302001", 180000.0, "UNEMPLOYED",
                false, true, "PENDING", today
        );

        createCitizenIfAbsent(
                "456789012345", "DEFGH4567I", "Anjali", "Deshmukh",
                "anjali.deshmukh@email.com", "9876543213", "9988776655003",
                LocalDate.of(1988, 11, 30), "FEMALE", "321 Elm Street",
                "Karnataka", "Bangalore", "560001", 420000.0, "EMPLOYED",
                false, false, "VERIFIED", today
        );

        createCitizenIfAbsent(
                "567890123456", "EFGHI5678J", "Vikram", "Rao",
                "vikram.rao@email.com", "9876543214", "9988776655004",
                LocalDate.of(1982, 7, 18), "MALE", "654 Birch Lane",
                "Telangana", "Hyderabad", "500001", 290000.0, "UNEMPLOYED",
                false, true, "UNDER_REVIEW", today
        );

        createCitizenIfAbsent(
                "678901234567", "FGHIJ6789K", "Kiran", "Mehta",
                "kiran.mehta@email.com", "9876543215", "9988776655005",
                LocalDate.of(1992, 1, 12), "FEMALE", "12 River View",
                "Gujarat", "Surat", "395001", 210000.0, "SELF_EMPLOYED",
                false, true, "PENDING", today
        );

        createCitizenIfAbsent(
                "789012345678", "GHIJK7890L", "Arun", "Nair",
                "arun.nair@email.com", "9876543216", "9988776655006",
                LocalDate.of(1987, 9, 3), "MALE", "44 Lake Road",
                "Kerala", "Kochi", "682001", 460000.0, "EMPLOYED",
                false, false, "VERIFIED", today
        );
    }

    private void createCitizenIfAbsent(String aadhaar, String pan, String first, String last,
                                       String email, String phone, String bankAcc,
                                       LocalDate dob, String gender, String address,
                                       String state, String district, String pincode,
                                       Double income, String empStatus, Boolean isGov,
                                       Boolean isBpl, String status, LocalDate today) {
        if (citizenRepository.findByAadhaar(aadhaar).isPresent() || citizenRepository.findByEmail(email).isPresent()) {
            return;
        }
        String ugid = UgidGenerator.generateFromAadhaar(aadhaar);
        Citizen c = new Citizen(
                null, ugid, aadhaar, pan, first, last, email, phone, bankAcc,
                dob, gender, address, state, district, pincode, income,
                empStatus, isGov, isBpl, true, today, today, null, status
        );
        citizenRepository.save(c);
    }

    private void seedProjects() {
        if (projectRepository.count() > 0) {
            return;
        }
        logger.info("Seeding initial projects...");
        LocalDate today = LocalDate.now();

        Project p1 = new Project(
                null, "PROJ-001", "NH44 Highway Expansion Phase 2",
                "Expansion and widening of National Highway 44 in Gujarat", "ROAD",
                "Gujarat", "Ahmedabad", "Ashok Gehlot", "Amit Shah",
                new BigDecimal("500000000.00"), new BigDecimal("200000000.00"), new BigDecimal("150000000.00"),
                "ONGOING", "OFFICER_REVIEW", null, "APPROVED", null,
                65, LocalDate.of(2023, 1, 15), LocalDate.of(2025, 12, 31),
                "GOOD", today, today, null
        );

        Project p2 = new Project(
                null, "PROJ-002", "Rural Water Supply Project",
                "Installation of piped water supply in 50 villages", "WATER",
                "Rajasthan", "Jaipur", "Vasundhara Raje", "Gumansingh Lodha",
                new BigDecimal("250000000.00"), new BigDecimal("100000000.00"), new BigDecimal("80000000.00"),
                "ONGOING", "OFFICER_REVIEW", null, "APPROVED", null,
                45, LocalDate.of(2023, 6, 1), LocalDate.of(2026, 3, 31),
                "GOOD", today, today, null
        );

        Project p3 = new Project(
                null, "PROJ-003", "Smart City Development",
                "Smart city infrastructure development in Mumbai", "INFRASTRUCTURE",
                "Maharashtra", "Mumbai", "Raj Thackeray", "Sharad Pawar",
                new BigDecimal("1000000000.00"), new BigDecimal("350000000.00"), new BigDecimal("250000000.00"),
                "ONGOING", "OFFICER_REVIEW", null, "APPROVED", null,
                55, LocalDate.of(2022, 12, 1), LocalDate.of(2026, 11, 30),
                "SATISFACTORY", today, today, null
        );

        Project p4 = new Project(
                null, "PROJ-004", "School Building Construction",
                "Construction of 100 new school buildings in rural areas", "EDUCATION",
                "Telangana", "Hyderabad", "K. Chandrasekhar Rao", "Rajinikanth",
                new BigDecimal("400000000.00"), BigDecimal.ZERO, BigDecimal.ZERO,
                "PENDING", "ADMIN_REVIEW", null, null, null,
                0, LocalDate.of(2024, 3, 1), LocalDate.of(2026, 2, 28),
                "PENDING", today, today, null
        );

        Project p5 = new Project(
                null, "PROJ-005", "District Hospital Upgrade",
                "Modernization of district hospital equipment and wards", "HEALTH",
                "Maharashtra", "Pune", "Ajit Pawar", "Supriya Sule",
                new BigDecimal("300000000.00"), new BigDecimal("70000000.00"), new BigDecimal("45000000.00"),
                "ONGOING", "OFFICER_REVIEW", null, "APPROVED", null,
                28, LocalDate.of(2024, 1, 1), LocalDate.of(2026, 6, 30),
                "GOOD", today, today, null
        );

        Project p6 = new Project(
                null, "PROJ-006", "Village Solar Lighting",
                "Solar street lighting in 120 villages", "ENERGY",
                "Rajasthan", "Udaipur", "Gulab Chand", "CP Joshi",
                new BigDecimal("120000000.00"), new BigDecimal("20000000.00"), new BigDecimal("10000000.00"),
                "ONGOING", "OFFICER_REVIEW", null, "APPROVED", null,
                18, LocalDate.of(2024, 2, 1), LocalDate.of(2025, 12, 31),
                "SATISFACTORY", today, today, null
        );

        projectRepository.saveAll(Arrays.asList(p1, p2, p3, p4, p5, p6));
        logger.info("Successfully seeded 6 projects.");
    }

    private void seedEnrollments() {
        if (enrollmentRepository.count() > 0) {
            return;
        }
        logger.info("Seeding initial enrollments...");
        List<Citizen> citizens = citizenRepository.findAll();
        List<Scheme> schemes = schemeRepository.findAll();

        if (citizens.isEmpty() || schemes.isEmpty()) {
            return;
        }

        LocalDateTime now = LocalDateTime.now();

        Citizen c1 = citizens.get(0);
        Citizen c2 = citizens.size() > 1 ? citizens.get(1) : c1;
        Citizen c3 = citizens.size() > 2 ? citizens.get(2) : c1;
        Citizen c4 = citizens.size() > 3 ? citizens.get(3) : c1;
        Citizen c5 = citizens.size() > 4 ? citizens.get(4) : c1;

        Scheme s1 = schemes.get(0);
        Scheme s2 = schemes.size() > 1 ? schemes.get(1) : s1;
        Scheme s3 = schemes.size() > 2 ? schemes.get(2) : s1;
        Scheme s4 = schemes.size() > 3 ? schemes.get(3) : s1;
        Scheme s5 = schemes.size() > 4 ? schemes.get(4) : s1;

        Enrollment e1 = new Enrollment(
                null, c1, s1, "ENR-001-2024-UUID001", "APPROVED", "CLOSED",
                now.minusDays(60), now.minusDays(59), null, "APPROVED", "APPROVED", "APPROVED",
                "ELIGIBLE", false, "LOW", 5, false, null, null, null, now, now
        );

        Enrollment e2 = new Enrollment(
                null, c2, s3, "ENR-002-2024-UUID002", "APPROVED", "CLOSED",
                now.minusDays(50), now.minusDays(49), null, "APPROVED", "APPROVED", "APPROVED",
                "ELIGIBLE", false, "LOW", 8, false, null, null, null, now, now
        );

        Enrollment e3 = new Enrollment(
                null, c3, s2, "ENR-003-2024-UUID003", "APPLIED", "OFFICER_REVIEW",
                now.minusDays(30), null, null, null, null, null,
                "ELIGIBLE", false, "MEDIUM", 35, false, null, null, null, now, now
        );

        Enrollment e4 = new Enrollment(
                null, c4, s4, "ENR-004-2024-UUID004", "APPROVED", "CLOSED",
                now.minusDays(20), now.minusDays(19), null, "APPROVED", "APPROVED", "APPROVED",
                "ELIGIBLE", false, "LOW", 12, false, null, null, null, now, now
        );

        Enrollment e5 = new Enrollment(
                null, c5, s5, "ENR-005-2024-UUID005", "FLAGGED", "AUDITOR_REVIEW",
                now.minusDays(10), null, null, null, null, null,
                "ELIGIBLE", true, "HIGH", 65, true, "FLAG",
                "High risk score anomaly detected across multiple schemes", null, now, now
        );

        enrollmentRepository.saveAll(Arrays.asList(e1, e2, e3, e4, e5));
        logger.info("Successfully seeded 5 enrollments.");
    }

    private void seedAuditLogs() {
        if (auditLogRepository.count() > 0) {
            return;
        }
        logger.info("Seeding initial audit logs...");
        LocalDateTime now = LocalDateTime.now();

        AuditLog a1 = new AuditLog(null, "CREATE", "CITIZEN", 1L, "admin@govshield.gov.in", "New citizen registered: Ramesh Patel", "SUCCESS", "192.168.1.100", now.minusDays(5));
        AuditLog a2 = new AuditLog(null, "APPLY", "ENROLLMENT", 1L, "9876543210", "Citizen applied for scheme: PM-JAY-001", "SUCCESS", "192.168.1.101", now.minusDays(4));
        AuditLog a3 = new AuditLog(null, "APPROVE", "ENROLLMENT", 1L, "officer1@govshield.gov.in", "Enrollment approved after eligibility verification", "SUCCESS", "192.168.1.102", now.minusDays(3));
        AuditLog a4 = new AuditLog(null, "UPDATE", "PROJECT", 1L, "officer1@govshield.gov.in", "Project progress updated to 65%", "SUCCESS", "192.168.1.103", now.minusDays(2));
        AuditLog a5 = new AuditLog(null, "FRAUD_FLAG", "ENROLLMENT", 5L, "auditor@govshield.gov.in", "Enrollment flagged for fraud investigation - High risk score", "SUCCESS", "192.168.1.105", now.minusDays(1));

        auditLogRepository.saveAll(Arrays.asList(a1, a2, a3, a4, a5));
        logger.info("Successfully seeded 5 audit logs.");
    }

    private void seedCitizenDocuments() {
        if (citizenDocumentRepository.count() > 0) {
            return;
        }
        List<Citizen> citizens = citizenRepository.findAll();
        if (citizens.isEmpty()) return;

        Citizen c1 = citizens.get(0);
        LocalDateTime now = LocalDateTime.now();

        CitizenDocument d1 = new CitizenDocument(
                null, c1, "AADHAAR", "123456789012", "aadhaar-ramesh.txt",
                "U2FtcGxlIEFhZGhhYXIgRG9jdW1lbnQ=", "VERIFIED",
                "Cross-verified from UIDAI feed", "OFFICER", now.minusDays(30), now.minusDays(29)
        );

        CitizenDocument d2 = new CitizenDocument(
                null, c1, "PAN", "ABCDE1234F", "pan-ramesh.txt",
                "U2FtcGxlIFBBTiBEb2N1bWVudA==", "VERIFIED",
                "PAN name and number matched", "ADMIN", now.minusDays(30), now.minusDays(29)
        );

        citizenDocumentRepository.saveAll(Arrays.asList(d1, d2));
        logger.info("Successfully seeded citizen documents.");
    }

    private void seedCepsProfiles() {
        if (citizenEconomicProfileRepository.count() > 0) {
            return;
        }
        List<Citizen> citizens = citizenRepository.findAll();
        if (citizens.isEmpty()) return;

        LocalDateTime now = LocalDateTime.now();

        for (int i = 0; i < citizens.size() && i < 4; i++) {
            Citizen c = citizens.get(i);
            int incomeScore = (i + 1) * 6;
            int assetScore = (i + 1) * 4;
            int landScore = (i + 1) * 4;
            int empScore = (i + 1) * 2;
            int utilScore = (i + 1) * 3;
            int ceps = incomeScore + assetScore + landScore + empScore + utilScore;
            String category = ceps < 35 ? "POOR_ELIGIBLE" : (ceps < 70 ? "MODERATE_SUPPORT" : "NOT_ELIGIBLE");
            String factors = String.format("{\"vehicles\":%d,\"landAcres\":%.1f,\"electricityUnitsMonthly\":%d}", i, i * 0.8, 80 + i * 60);

            CitizenEconomicProfile profile = new CitizenEconomicProfile(
                    null, c, incomeScore, assetScore, landScore, empScore, utilScore, ceps, category, factors, now
            );
            citizenEconomicProfileRepository.save(profile);
        }
        logger.info("Successfully seeded citizen CEPS profiles.");
    }
}
