package com.govshield.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.time.LocalDateTime;

@Entity
@Table(name = "fraud_flags")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class FraudFlag {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @OneToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "citizen_id", nullable = false, unique = true)
    private Citizen citizen;

    @Column(name = "risk_score", nullable = false)
    private Integer riskScore;

    @Column(name = "risk_level", nullable = false, length = 20)
    private String riskLevel;

    @Column(name = "duplicate_aadhaar", nullable = false)
    private Boolean duplicateAadhaar;

    @Column(name = "duplicate_bank_account", nullable = false)
    private Boolean duplicateBankAccount;

    @Column(name = "income_mismatch", nullable = false)
    private Boolean incomeMismatch;

    @Column(name = "scheme_conflict", nullable = false)
    private Boolean schemeConflict;

    @Column(length = 1000)
    private String summary;

    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt;

    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;
}
