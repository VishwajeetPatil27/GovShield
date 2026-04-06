package com.govshield.service;

import com.govshield.model.Enrollment;
import com.govshield.repository.EnrollmentRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class FraudDetectionService {

    @Autowired
    private EnrollmentRepository enrollmentRepository;

    /**
     * Detect fraud patterns for a citizen based on enrollments
     */
    public List<Enrollment> detectFraudPatterns(Long citizenId) {
        List<Enrollment> enrollments = enrollmentRepository.findByCitizenId(citizenId);

        return enrollments.stream()
            .filter(enrollment -> {
                // Check multiple risk factors
                String riskLevel = enrollment.getFraudRiskLevel();
                boolean hasHighRisk = "HIGH".equals(riskLevel);
                
                // Check for rapid applications
                long rapidAppCount = enrollments.stream()
                    .filter(e -> java.time.temporal.ChronoUnit.DAYS.between(
                        e.getApplicationDate(), enrollment.getApplicationDate()) < 15)
                    .count();
                boolean hasRapidApplications = rapidAppCount > 2;

                // Check for duplicate sector enrollments
                long sectorCount = enrollments.stream()
                    .filter(e -> e.getScheme().getSector().equals(enrollment.getScheme().getSector()))
                    .count();
                boolean hasDuplicateSectors = sectorCount > 1;

                return hasHighRisk || hasRapidApplications || hasDuplicateSectors;
            })
            .collect(Collectors.toList());
    }

    /**
     * Flag enrollment as potential fraud
     */
    public Enrollment flagAsfraud(Long enrollmentId, String fraudReason) {
        Enrollment enrollment = enrollmentRepository.findById(enrollmentId)
            .orElseThrow(() -> new IllegalArgumentException("Enrollment not found"));

        enrollment.setIsFraudDetected(true);
        enrollment.setFraudRiskLevel("HIGH");
        enrollment.setStatus("FLAGGED");
        enrollment.setUpdatedAt(LocalDateTime.now());

        return enrollmentRepository.save(enrollment);
    }

    /**
     * Bulk fraud detection for all active enrollments
     */
    public List<Enrollment> detectAllFrauds() {
        List<Enrollment> allEnrollments = enrollmentRepository.findAll();

        return allEnrollments.stream()
            .filter(enrollment -> {
                String riskLevel = enrollment.getFraudRiskLevel();
                return "HIGH".equals(riskLevel) || "MEDIUM".equals(riskLevel);
            })
            .collect(Collectors.toList());
    }

    /**
     * Get fraud alerts for dashboard
     */
    public List<Enrollment> getFraudAlerts() {
        return enrollmentRepository.findAll().stream()
            .filter(enrollment -> 
                enrollment.getIsFraudDetected() || 
                ("HIGH".equals(enrollment.getFraudRiskLevel()) && !enrollment.getStatus().equals("REJECTED"))
            )
            .collect(Collectors.toList());
    }
}
