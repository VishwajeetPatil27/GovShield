package com.govshield.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.time.LocalDateTime;

@Entity
@Table(name = "project_updates")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class ProjectUpdate {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "project_id", nullable = false)
    private Project project;

    @Column(nullable = false, length = 20)
    private String submittedByRole; // OFFICER|ADMIN|CONTRACTOR (demo)

    @Column(length = 100)
    private String submittedByIdentifier;

    @Column(nullable = false)
    private Integer reportedProgress = 0;

    @Column(length = 1000)
    private String message;

    @Lob
    @Column(columnDefinition = "LONGTEXT")
    private String photoBase64;

    private Double geoLat;

    private Double geoLng;

    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt = LocalDateTime.now();
}

