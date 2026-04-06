package com.govshield.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.time.LocalDateTime;

@Entity
@Table(name = "project_alerts")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class ProjectAlert {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "project_id", nullable = false)
    private Project project;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "evidence_id")
    private ProjectEvidence evidence;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "update_id")
    private ProjectUpdate update;

    @Column(nullable = false, length = 10)
    private String severity = "LOW"; // LOW|MEDIUM|HIGH

    @Column(nullable = false, length = 500)
    private String reason;

    @Column(nullable = false)
    private Boolean resolved = false;

    @Column(length = 20)
    private String resolvedByRole;

    @Column(length = 100)
    private String resolvedByIdentifier;

    private LocalDateTime resolvedAt;

    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt = LocalDateTime.now();
}

