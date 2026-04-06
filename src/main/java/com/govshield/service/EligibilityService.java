package com.govshield.service;

import com.govshield.dto.EligibilityResponse;
import com.govshield.dto.RealtimeEligibilityRequest;
import com.govshield.dto.RealtimeEligibilityResponse;
import com.govshield.dto.RealtimeSchemeResult;
import com.govshield.dto.SchemeApplyRequest;
import com.govshield.exception.CustomException;
import com.govshield.model.Citizen;
import com.govshield.model.Enrollment;
import com.govshield.model.Scheme;
import com.govshield.repository.CitizenRepository;
import com.govshield.repository.EnrollmentRepository;
import com.govshield.repository.SchemeRepository;
import com.govshield.repository.CitizenEconomicProfileRepository;
import com.govshield.util.EligibilityRules;
import com.govshield.util.RiskCalculator;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import java.time.LocalDateTime;
import java.util.List;
import java.util.ArrayList;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
public class EligibilityService {

    @Autowired
    private CitizenRepository citizenRepository;

    @Autowired
    private SchemeRepository schemeRepository;

    @Autowired
    private EnrollmentRepository enrollmentRepository;

    @Autowired
    private SchemeConflictService schemeConflictService;

    @Autowired
    private CepsService cepsService;

    @Autowired
    private CitizenEconomicProfileRepository economicProfileRepository;

    /**
     * Apply for a scheme and check eligibility
     */
    public EligibilityResponse applyForScheme(SchemeApplyRequest request) {
        Citizen citizen = citizenRepository.findByUgid(request.getUgid())
            .orElseThrow(() -> new CustomException("Citizen not found", "CITIZEN_NOT_FOUND", 404));

        Scheme scheme = schemeRepository.findById(request.getSchemeId())
            .orElseThrow(() -> new CustomException("Scheme not found", "SCHEME_NOT_FOUND", 404));

        // Get all enrollments for this citizen
        List<Enrollment> citizenEnrollments = enrollmentRepository.findByCitizenId(citizen.getId());

        List<String> activeSectors = citizenEnrollments.stream()
            .filter(e -> e.getScheme() != null && e.getScheme().getSector() != null)
            .filter(e -> e.getStatus() == null || (
                !e.getStatus().equalsIgnoreCase("REJECTED") &&
                    !e.getStatus().equalsIgnoreCase("AUDITOR_REJECTED") &&
                    !e.getStatus().equalsIgnoreCase("OFFICER_REJECTED")))
            .map(e -> e.getScheme().getSector())
            .collect(Collectors.toList());

        SchemeConflictService.ConflictResult conflict = schemeConflictService
            .findConflict(scheme.getSector(), activeSectors)
            .orElseGet(() -> {
                boolean sameSector = activeSectors.stream().anyMatch(s -> s != null && s.equalsIgnoreCase(scheme.getSector()));
                if (sameSector) {
                    return new SchemeConflictService.ConflictResult(null, "REJECT",
                        "Citizen already has an application/benefit in this sector. Only one scheme per sector is allowed.");
                }
                return null;
            });

        if (conflict != null && "REJECT".equalsIgnoreCase(conflict.getAction())) {
            return createEligibilityResponse(null, scheme, "INELIGIBLE", "LOW", conflict.getMessage());
        }

        // Check eligibility rules
        String eligibilityStatus = "ELIGIBLE";
        String eligibilityNotes = "";

        if (!EligibilityRules.isEligible(citizen, scheme)) {
            eligibilityStatus = "INELIGIBLE";
            if (EligibilityRules.hasIncomeViolation(citizen, scheme)) {
                eligibilityNotes += "Income exceeds limit. ";
            }
            if (EligibilityRules.hasEmploymentViolation(citizen, scheme)) {
                eligibilityNotes += "Government employees not eligible. ";
            }
        }

        // CEPS-based policy (optional per scheme)
        if (Boolean.TRUE.equals(scheme.getUsesCeps())) {
            int ceps = ensureCepsScore(citizen);
            if (ceps < safeInt(scheme.getMinCepsScore(), 0) || ceps > safeInt(scheme.getMaxCepsScore(), 100)) {
                eligibilityStatus = "INELIGIBLE";
                eligibilityNotes += "CEPS score out of allowed range. ";
            }
        }

        // Create enrollment record
        Enrollment enrollment = new Enrollment();
        enrollment.setCitizen(citizen);
        enrollment.setScheme(scheme);
        enrollment.setEnrollmentNumber("ENR-" + UUID.randomUUID().toString());
        enrollment.setApplicationDate(LocalDateTime.now());
        enrollment.setStatus("SUBMITTED");
        enrollment.setCurrentStage("OFFICER_REVIEW");
        enrollment.setEligibilityStatus(eligibilityStatus);
        enrollment.setIsFraudDetected(false);
        if (conflict != null && "FLAG".equalsIgnoreCase(conflict.getAction())) {
            enrollment.setConflictFlag(true);
            enrollment.setConflictAction(conflict.getAction());
            enrollment.setConflictMessage(conflict.getMessage());
            enrollment.setConflictRuleId(conflict.getRuleId());
            enrollment.setEligibilityStatus("FLAGGED");
        }
        enrollment.setCreatedAt(LocalDateTime.now());
        enrollment.setUpdatedAt(LocalDateTime.now());

        // Calculate fraud risk
        int fraudRiskScore = RiskCalculator.calculateFraudRiskScore(enrollment, citizenEnrollments);
        String fraudRiskLevel = RiskCalculator.calculateFraudRiskLevelFromScore(fraudRiskScore);
        enrollment.setFraudRiskLevel(fraudRiskLevel);
        enrollment.setFraudRiskScore(fraudRiskScore);

        // Check if should flag for audit
        if (RiskCalculator.shouldFlagForAudit(fraudRiskLevel, citizenEnrollments) || Boolean.TRUE.equals(enrollment.getConflictFlag())) {
            enrollment.setStatus("FLAGGED_FOR_AUDIT");
            enrollment.setEligibilityStatus("FLAGGED");
        }

        Enrollment savedEnrollment = enrollmentRepository.save(enrollment);

        return createEligibilityResponse(savedEnrollment, scheme, eligibilityStatus, fraudRiskLevel,
            eligibilityNotes);
    }

    /**
     * Check eligibility without creating enrollment
     */
    public EligibilityResponse checkEligibility(String ugid, Long schemeId) {
        Citizen citizen = citizenRepository.findByUgid(ugid)
            .orElseThrow(() -> new CustomException("Citizen not found", "CITIZEN_NOT_FOUND", 404));

        Scheme scheme = schemeRepository.findById(schemeId)
            .orElseThrow(() -> new CustomException("Scheme not found", "SCHEME_NOT_FOUND", 404));

        List<Enrollment> citizenEnrollments = enrollmentRepository.findByCitizenId(citizen.getId());

        List<String> activeSectors = citizenEnrollments.stream()
            .filter(e -> e.getScheme() != null && e.getScheme().getSector() != null)
            .filter(e -> e.getStatus() == null || (
                !e.getStatus().equalsIgnoreCase("REJECTED") &&
                    !e.getStatus().equalsIgnoreCase("AUDITOR_REJECTED") &&
                    !e.getStatus().equalsIgnoreCase("OFFICER_REJECTED")))
            .map(e -> e.getScheme().getSector())
            .collect(Collectors.toList());

        SchemeConflictService.ConflictResult conflict = schemeConflictService
            .findConflict(scheme.getSector(), activeSectors)
            .orElseGet(() -> {
                boolean sameSector = activeSectors.stream().anyMatch(s -> s != null && s.equalsIgnoreCase(scheme.getSector()));
                if (sameSector) {
                    return new SchemeConflictService.ConflictResult(null, "REJECT",
                        "Citizen already has an application/benefit in this sector. Only one scheme per sector is allowed.");
                }
                return null;
            });

        boolean eligible = EligibilityRules.isEligible(citizen, scheme);
        String message = eligible ? "Citizen is eligible" : "Citizen does not meet eligibility criteria";

        if (eligible && conflict != null) {
            eligible = false;
            message = conflict.getMessage();
        }

        if (eligible && Boolean.TRUE.equals(scheme.getUsesCeps())) {
            int ceps = ensureCepsScore(citizen);
            if (ceps < safeInt(scheme.getMinCepsScore(), 0) || ceps > safeInt(scheme.getMaxCepsScore(), 100)) {
                eligible = false;
                message = "CEPS score out of allowed range";
            }
        }

        String eligibilityStatus = eligible ? "ELIGIBLE" : "INELIGIBLE";

        return new EligibilityResponse(null, null, eligibilityStatus, "LOW",
            message, eligible);
    }

    /**
     * Real-time eligibility checker:
     * - If UGID is provided: uses citizen data + CEPS profile + conflict rules.
     * - If UGID is not provided: uses supplied inputs (income/age/gov employee + CEPS factors) to simulate eligibility.
     */
    public RealtimeEligibilityResponse realtimeCheck(RealtimeEligibilityRequest request) {
        List<Scheme> schemes = schemeRepository.findByIsActive(true);
        List<RealtimeSchemeResult> results = new ArrayList<>();

        Integer cepsScore = null;
        String cepsCategory = null;

        Citizen citizen = null;
        List<String> activeSectors = List.of();
        if (request.getUgid() != null && !request.getUgid().isBlank()) {
            citizen = citizenRepository.findByUgid(request.getUgid())
                .orElseThrow(() -> new CustomException("Citizen not found", "CITIZEN_NOT_FOUND", 404));
            cepsScore = ensureCepsScore(citizen);
            cepsCategory = economicProfileRepository.findByCitizenId(citizen.getId())
                .map(p -> p.getCepsCategory())
                .orElse(null);
            List<Enrollment> citizenEnrollments = enrollmentRepository.findByCitizenId(citizen.getId());
            activeSectors = citizenEnrollments.stream()
                .filter(e -> e.getScheme() != null && e.getScheme().getSector() != null)
                .filter(e -> e.getStatus() == null || !e.getStatus().equalsIgnoreCase("REJECTED"))
                .map(e -> e.getScheme().getSector())
                .collect(Collectors.toList());
        } else {
            cepsScore = computeCepsFromRequest(request);
            cepsCategory = cepsScore == null ? null : (cepsScore <= 30 ? "POOR_ELIGIBLE" : (cepsScore <= 60 ? "MODERATE_SUPPORT" : "NOT_ELIGIBLE"));
        }

        for (Scheme scheme : schemes) {
            boolean eligible = true;
            String reason = "Eligible";

            if (citizen != null) {
                if (!EligibilityRules.isEligible(citizen, scheme)) {
                    eligible = false;
                    reason = "Failed base eligibility rules (income/age/employment)";
                }
            } else {
                Long income = request.getAnnualIncome();
                Integer age = request.getAge();
                Boolean isGov = request.getIsGovernmentEmployee();

                if (income != null && scheme.getMaxAnnualIncome() != null && income.doubleValue() > scheme.getMaxAnnualIncome()) {
                    eligible = false;
                    reason = "Income exceeds limit";
                }
                if (eligible && age != null && scheme.getMinAge() != null && scheme.getMaxAge() != null) {
                    if (age < scheme.getMinAge() || age > scheme.getMaxAge()) {
                        eligible = false;
                        reason = "Age out of range";
                    }
                }
                if (eligible && Boolean.TRUE.equals(isGov) && Boolean.FALSE.equals(scheme.getIsGovernmentEmployeeEligible())) {
                    eligible = false;
                    reason = "Government employees not eligible";
                }
            }

            if (eligible && cepsScore != null && Boolean.TRUE.equals(scheme.getUsesCeps())) {
                int min = safeInt(scheme.getMinCepsScore(), 0);
                int max = safeInt(scheme.getMaxCepsScore(), 100);
                if (cepsScore < min || cepsScore > max) {
                    eligible = false;
                    reason = "CEPS score out of allowed range";
                }
            }

            if (eligible && citizen != null) {
                SchemeConflictService.ConflictResult conflict = schemeConflictService
                    .findConflict(scheme.getSector(), activeSectors)
                    .orElse(null);
                if (conflict != null) {
                    eligible = false;
                    reason = conflict.getMessage();
                }
            }

            results.add(new RealtimeSchemeResult(
                scheme.getId(),
                scheme.getSchemeCode(),
                scheme.getSchemeName(),
                scheme.getSector(),
                eligible,
                reason
            ));
        }

        return new RealtimeEligibilityResponse(cepsScore, cepsCategory, results);
    }

    private int ensureCepsScore(Citizen citizen) {
        return economicProfileRepository.findByCitizenId(citizen.getId())
            .map(p -> p.getCepsScore())
            .orElseGet(() -> cepsService.calculateAndUpsert(citizen.getUgid(), new com.govshield.dto.CepsCalculateRequest()).getCepsScore());
    }

    private Integer computeCepsFromRequest(RealtimeEligibilityRequest request) {
        long income = request.getAnnualIncome() == null ? 0 : Math.max(0, request.getAnnualIncome());
        int incomeScore;
        if (income <= 100000) incomeScore = 3;
        else if (income <= 300000) incomeScore = 10;
        else if (income <= 600000) incomeScore = 16;
        else if (income <= 1200000) incomeScore = 22;
        else incomeScore = 25;

        int vehicles = request.getVehiclesCount() == null ? 0 : Math.max(0, request.getVehiclesCount());
        double assetsValue = request.getDeclaredAssetsValue() == null ? 0 : Math.max(0, request.getDeclaredAssetsValue());
        int vehicleScore = Math.min(15, vehicles * 4);
        int assetsScore = (int) Math.min(10, (assetsValue / 500000.0) * 5);
        int assetScore = Math.min(25, vehicleScore + assetsScore);

        double land = request.getLandAcres() == null ? 0 : Math.max(0, request.getLandAcres());
        int landScore = land == 0 ? 0 : (land <= 1 ? 5 : (land <= 3 ? 12 : (land <= 6 ? 17 : 20)));

        int utilityUnits = request.getElectricityUnitsMonthly() == null ? 0 : Math.max(0, request.getElectricityUnitsMonthly());
        int utilityScore = utilityUnits <= 100 ? 2 : (utilityUnits <= 200 ? 6 : (utilityUnits <= 350 ? 10 : 15));

        String emp = request.getEmploymentStatus() == null ? "" : request.getEmploymentStatus().trim().toUpperCase();
        int employmentScore;
        if (Boolean.TRUE.equals(request.getIsGovernmentEmployee())) employmentScore = 15;
        else if (emp.contains("UNEMPLOY")) employmentScore = 0;
        else if (emp.contains("SELF")) employmentScore = 6;
        else if (emp.contains("EMPLOY")) employmentScore = 10;
        else employmentScore = 5;

        int total = incomeScore + assetScore + landScore + employmentScore + utilityScore;
        if (total > 100) total = 100;
        if (total < 0) total = 0;
        return total;
    }

    private int safeInt(Integer value, int fallback) {
        return value == null ? fallback : value;
    }

    /**
     * Get enrollment by ID
     */
    public Enrollment getEnrollmentById(Long id) {
        return enrollmentRepository.findById(id)
            .orElseThrow(() -> new CustomException("Enrollment not found", "ENROLLMENT_NOT_FOUND", 404));
    }

    /**
     * Get all enrollments for citizen
     */
    public List<Enrollment> getCitizenEnrollments(Long citizenId) {
        return enrollmentRepository.findByCitizenId(citizenId);
    }

    /**
     * Get all enrollments
     */
    public List<Enrollment> getAllEnrollments() {
        return enrollmentRepository.findAll();
    }

    /**
     * Approve enrollment
     */
    public Enrollment approveEnrollment(Long enrollmentId) {
        Enrollment enrollment = getEnrollmentById(enrollmentId);
        return adminReview(enrollmentId, true, "Approved through legacy endpoint");
    }

    /**
     * Reject enrollment
     */
    public Enrollment rejectEnrollment(Long enrollmentId, String reason) {
        return adminReview(enrollmentId, false, reason);
    }

    public Enrollment auditorReview(Long enrollmentId, boolean approved, String remarks) {
        Enrollment enrollment = getEnrollmentById(enrollmentId);
        if (!"AUDITOR_REVIEW".equals(enrollment.getCurrentStage())) {
            throw new CustomException("Enrollment is not in auditor stage", "INVALID_STAGE", 400);
        }

        enrollment.setAuditorDecision(approved ? "APPROVED" : "REJECTED");
        if (approved) {
            enrollment.setStatus("AUDITOR_APPROVED");
            enrollment.setCurrentStage("OFFICER_REVIEW");
        } else {
            enrollment.setStatus("AUDITOR_REJECTED");
            enrollment.setCurrentStage("CLOSED");
            enrollment.setRejectionReason(remarks);
        }
        enrollment.setUpdatedAt(LocalDateTime.now());
        return enrollmentRepository.save(enrollment);
    }

    public Enrollment officerReview(Long enrollmentId, boolean approved, String remarks) {
        Enrollment enrollment = getEnrollmentById(enrollmentId);
        if (!"OFFICER_REVIEW".equals(enrollment.getCurrentStage()) &&
            !"AUDITOR_REVIEW".equals(enrollment.getCurrentStage())) {
            throw new CustomException("Enrollment is not in officer workflow stage", "INVALID_STAGE", 400);
        }

        enrollment.setOfficerDecision(approved ? "APPROVED" : "REJECTED");
        if (approved) {
            enrollment.setStatus("OFFICER_APPROVED");
            enrollment.setCurrentStage("ADMIN_REVIEW");
        } else {
            enrollment.setStatus("OFFICER_REJECTED");
            enrollment.setCurrentStage("CLOSED");
            enrollment.setRejectionReason(remarks);
        }
        enrollment.setUpdatedAt(LocalDateTime.now());
        return enrollmentRepository.save(enrollment);
    }

    public Enrollment adminReview(Long enrollmentId, boolean approved, String remarks) {
        Enrollment enrollment = getEnrollmentById(enrollmentId);
        if (!"ADMIN_REVIEW".equals(enrollment.getCurrentStage()) && !"SUBMITTED".equals(enrollment.getStatus())) {
            throw new CustomException("Enrollment is not in admin stage", "INVALID_STAGE", 400);
        }

        enrollment.setAdminDecision(approved ? "APPROVED" : "REJECTED");
        if (approved) {
            enrollment.setStatus("APPROVED");
            enrollment.setApprovalDate(LocalDateTime.now());
        } else {
            enrollment.setStatus("REJECTED");
            enrollment.setRejectionReason(remarks);
        }
        enrollment.setCurrentStage("CLOSED");
        enrollment.setUpdatedAt(LocalDateTime.now());
        return enrollmentRepository.save(enrollment);
    }

    /**
     * Helper method to create EligibilityResponse
     */
    private EligibilityResponse createEligibilityResponse(Enrollment enrollment, Scheme scheme,
                                                         String eligibilityStatus, String fraudRiskLevel,
                                                         String message) {
        return new EligibilityResponse(
            enrollment != null ? enrollment.getId() : null,
            enrollment != null ? enrollment.getEnrollmentNumber() : null,
            eligibilityStatus,
            fraudRiskLevel,
            message,
            "ELIGIBLE".equals(eligibilityStatus)
        );
    }
}
