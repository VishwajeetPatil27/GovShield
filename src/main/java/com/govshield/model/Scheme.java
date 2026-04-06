package com.govshield.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Entity
@Table(name = "schemes")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Scheme {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(unique = true, nullable = false)
    private String schemeCode;

    @Column(nullable = false)
    private String schemeName;

    @Column(length = 2000)
    private String description;

    @Column(nullable = false)
    private String sector; // EDUCATION, HEALTH, AGRICULTURE, HOUSING, etc.

    @Column(nullable = false)
    private String schemeType; // FINANCIAL, SUBSIDY, PROVISION, etc.

    @Column(nullable = false)
    private Double benefitAmount;

    @Column(nullable = false)
    private Double maxAnnualIncome;

    @Column(nullable = false)
    private Integer maxAge;

    @Column(nullable = false)
    private Integer minAge;

    @Column(nullable = false)
    private Boolean isGovernmentEmployeeEligible;

    @Column(name = "uses_ceps", nullable = false)
    private Boolean usesCeps = false;

    @Column(name = "min_ceps_score", nullable = false)
    private Integer minCepsScore = 0;

    @Column(name = "max_ceps_score", nullable = false)
    private Integer maxCepsScore = 100;

    @Column(nullable = false)
    private Boolean isActive;

    @Column(nullable = false)
    private LocalDate launchDate;

    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt;

    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;
}
