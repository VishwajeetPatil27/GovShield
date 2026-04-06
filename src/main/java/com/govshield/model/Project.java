package com.govshield.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.time.LocalDate;
import java.math.BigDecimal;

@Entity
@Table(name = "projects")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Project {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(unique = true, nullable = false)
    private String projectCode;

    @Column(nullable = false)
    private String projectName;

    @Column(length = 2000)
    private String description;

    @Column(nullable = false)
    private String category; // ROAD, SCHOOL, HOSPITAL, WATER, etc.

    @Column(nullable = false)
    private String state;

    @Column(nullable = false)
    private String district;

    @Column(nullable = false)
    private String allocatedMla;

    @Column(nullable = false)
    private String allocatedMp;

    @Column(nullable = false, precision = 15, scale = 2)
    private BigDecimal totalBudget;

    @Column(nullable = false, precision = 15, scale = 2)
    private BigDecimal releasedAmount;

    @Column(nullable = false, precision = 15, scale = 2)
    private BigDecimal spentAmount;

    @Column(nullable = false)
    private String status; // PLANNED, ONGOING, COMPLETED, SUSPENDED

    @Column
    private String currentStage; // AUDITOR_REVIEW, OFFICER_REVIEW, ADMIN_REVIEW, CLOSED

    @Column(length = 20)
    private String auditorDecision; // APPROVED, REJECTED

    @Column(length = 20)
    private String officerDecision; // APPROVED, REJECTED

    @Column(length = 20)
    private String adminDecision; // APPROVED, REJECTED

    @Column(nullable = false)
    private Integer progressPercentage;

    @Column(nullable = false)
    private LocalDate startDate;

    @Column(nullable = false)
    private LocalDate endDate;

    @Column(nullable = false)
    private String qualityStatus; // GOOD, AVERAGE, POOR

    @Column(nullable = false)
    private LocalDate createdAt;

    @Column(nullable = false)
    private LocalDate updatedAt;

    @Column(length = 500)
    private String remarks;
}
