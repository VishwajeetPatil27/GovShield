package com.govshield.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.time.LocalDateTime;

@Entity
@Table(name = "enrollments")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Enrollment {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "citizen_id", nullable = false)
    private Citizen citizen;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "scheme_id", nullable = false)
    private Scheme scheme;

    @Column(unique = true, nullable = false)
    private String enrollmentNumber;

    @Column(nullable = false)
    private String status; // APPLIED, APPROVED, REJECTED, ACTIVE, INACTIVE

    @Column(name = "current_stage")
    private String currentStage; // AUDITOR_REVIEW, OFFICER_REVIEW, ADMIN_REVIEW, CLOSED

    @Column(name = "applied_at", nullable = false)
    private LocalDateTime applicationDate;

    @Column(name = "approved_at")
    private LocalDateTime approvalDate;

    @Column(length = 1000)
    private String rejectionReason;

    @Column(length = 20)
    private String auditorDecision; // APPROVED, REJECTED

    @Column(length = 20)
    private String officerDecision; // APPROVED, REJECTED

    @Column(length = 20)
    private String adminDecision; // APPROVED, REJECTED

    @Column(nullable = false)
    private String eligibilityStatus; // ELIGIBLE, INELIGIBLE, FLAGGED

    @Column(nullable = false)
    private Boolean isFraudDetected;

    @Column(length = 500)
    private String fraudRiskLevel; // LOW, MEDIUM, HIGH

    @Column(name = "fraud_risk_score")
    private Integer fraudRiskScore = 0;

    @Column(name = "conflict_flag", nullable = false)
    private Boolean conflictFlag = false;

    @Column(name = "conflict_action")
    private String conflictAction; // REJECT|FLAG

    @Column(name = "conflict_message", length = 500)
    private String conflictMessage;

    @Column(name = "conflict_rule_id")
    private Long conflictRuleId;

    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt;

    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;
}
