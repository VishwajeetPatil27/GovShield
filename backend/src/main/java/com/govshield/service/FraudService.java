package com.govshield.service;

import com.govshield.dto.FraudRiskResponse;
import com.govshield.exception.CustomException;
import com.govshield.model.Citizen;
import com.govshield.model.Enrollment;
import com.govshield.model.FraudFlag;
import com.govshield.repository.CitizenRepository;
import com.govshield.repository.EnrollmentRepository;
import com.govshield.repository.FraudFlagRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;

@Service
public class FraudService {

    private final CitizenRepository citizenRepository;
    private final EnrollmentRepository enrollmentRepository;
    private final FraudFlagRepository fraudFlagRepository;

    public FraudService(CitizenRepository citizenRepository,
                        EnrollmentRepository enrollmentRepository,
                        FraudFlagRepository fraudFlagRepository) {
        this.citizenRepository = citizenRepository;
        this.enrollmentRepository = enrollmentRepository;
        this.fraudFlagRepository = fraudFlagRepository;
    }

    @Transactional
    public FraudRiskResponse analyzeCitizenFraud(Long citizenId) {
        Citizen citizen = citizenRepository.findById(citizenId)
            .orElseThrow(() -> new CustomException("Citizen not found", "CITIZEN_NOT_FOUND", 404));

        List<Enrollment> enrollments = enrollmentRepository.findByCitizenId(citizenId);
        List<Enrollment> activeEnrollments = enrollments.stream()
            .filter(this::isActiveEnrollment)
            .collect(Collectors.toList());

        boolean duplicateAadhaar = citizen.getAadhaar() != null
            && citizenRepository.countByAadhaar(citizen.getAadhaar()) > 1;

        boolean duplicateBankAccount = citizen.getBankAccountNumber() != null
            && !citizen.getBankAccountNumber().isBlank()
            && citizenRepository.countByBankAccountNumber(citizen.getBankAccountNumber()) > 1;

        boolean incomeMismatch = detectIncomeMismatch(citizen, activeEnrollments);
        boolean schemeConflict = detectSchemeConflict(activeEnrollments);

        int riskScore = 0;
        List<String> reasons = new ArrayList<>();

        if (duplicateAadhaar) {
            riskScore += 35;
            reasons.add("Duplicate Aadhaar found");
        }
        if (duplicateBankAccount) {
            riskScore += 30;
            reasons.add("Duplicate bank account found");
        }
        if (incomeMismatch) {
            riskScore += 20;
            reasons.add("Income mismatch with scheme rules");
        }
        if (schemeConflict) {
            riskScore += 25;
            reasons.add("Multiple scheme conflict detected");
        }

        riskScore = Math.min(riskScore, 100);
        String riskLevel = riskScore >= 70 ? "HIGH" : riskScore >= 40 ? "MEDIUM" : "LOW";
        String summary = reasons.isEmpty() ? "No major fraud indicators detected" : String.join("; ", reasons);

        FraudFlag flag = fraudFlagRepository.findByCitizen_Id(citizenId).orElseGet(FraudFlag::new);
        flag.setCitizen(citizen);
        flag.setRiskScore(riskScore);
        flag.setRiskLevel(riskLevel);
        flag.setDuplicateAadhaar(duplicateAadhaar);
        flag.setDuplicateBankAccount(duplicateBankAccount);
        flag.setIncomeMismatch(incomeMismatch);
        flag.setSchemeConflict(schemeConflict);
        flag.setSummary(summary);
        if (flag.getCreatedAt() == null) {
            flag.setCreatedAt(LocalDateTime.now());
        }
        flag.setUpdatedAt(LocalDateTime.now());
        fraudFlagRepository.save(flag);

        return new FraudRiskResponse(
            citizen.getId(),
            citizen.getFirstName() + " " + citizen.getLastName(),
            citizen.getAadhaar(),
            citizen.getBankAccountNumber(),
            riskScore,
            riskLevel,
            duplicateAadhaar,
            duplicateBankAccount,
            incomeMismatch,
            schemeConflict,
            reasons,
            flag.getUpdatedAt()
        );
    }

    private boolean detectIncomeMismatch(Citizen citizen, List<Enrollment> activeEnrollments) {
        if (citizen.getAnnualIncome() == null) {
            return false;
        }

        boolean exceedsSchemeLimits = activeEnrollments.stream()
            .map(Enrollment::getScheme)
            .filter(Objects::nonNull)
            .anyMatch(scheme -> scheme.getMaxAnnualIncome() != null
                && citizen.getAnnualIncome() > scheme.getMaxAnnualIncome());

        boolean povertyMismatch = Boolean.TRUE.equals(citizen.getIsBelowPovertyLine())
            && citizen.getAnnualIncome() > 300000;

        return exceedsSchemeLimits || povertyMismatch;
    }

    private boolean detectSchemeConflict(List<Enrollment> activeEnrollments) {
        if (activeEnrollments.size() < 2) {
            return false;
        }

        Map<String, Long> sectorCounts = activeEnrollments.stream()
            .map(Enrollment::getScheme)
            .filter(Objects::nonNull)
            .map(scheme -> scheme.getSector() == null ? "UNKNOWN" : scheme.getSector())
            .collect(Collectors.groupingBy(sector -> sector, LinkedHashMap::new, Collectors.counting()));

        boolean sameSectorConflict = sectorCounts.values().stream().anyMatch(count -> count > 1);
        boolean tooManyActiveSchemes = activeEnrollments.size() > 3;

        return sameSectorConflict || tooManyActiveSchemes;
    }

    private boolean isActiveEnrollment(Enrollment enrollment) {
        if (enrollment == null || enrollment.getStatus() == null) {
            return false;
        }

        String status = enrollment.getStatus().trim().toUpperCase();
        return !List.of("REJECTED", "FLAGGED", "INACTIVE", "CLOSED").contains(status);
    }
}
