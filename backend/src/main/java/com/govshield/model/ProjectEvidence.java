package com.govshield.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.time.LocalDateTime;

@Entity
@Table(name = "project_evidence")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class ProjectEvidence {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "project_id", nullable = false)
    private Project project;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "citizen_id")
    private Citizen citizen;

    @Column(nullable = false, length = 20)
    private String evidenceType; // PHOTO|COMPLAINT|REVIEW

    @Column(length = 2000)
    private String message;

    @Lob
    @Column(columnDefinition = "LONGTEXT")
    private String photoBase64;

    private Double geoLat;

    private Double geoLng;

    private Integer progressEstimate;

    private Integer contractorRating;

    @Column(nullable = false, length = 20)
    private String status = "NEW"; // NEW|VERIFIED|DISMISSED

    @Column(length = 20)
    private String reviewerRole;

    @Column(length = 100)
    private String reviewerIdentifier;

    @Column(length = 500)
    private String reviewerRemarks;

    private LocalDateTime reviewedAt;

    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt = LocalDateTime.now();
}

