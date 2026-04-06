package com.govshield.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.time.LocalDateTime;

@Entity
@Table(name = "scheme_conflict_rules")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class SchemeConflictRule {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 50)
    private String sectorA;

    @Column(nullable = false, length = 50)
    private String sectorB;

    @Column(nullable = false, length = 10)
    private String action = "REJECT"; // REJECT|FLAG

    @Column(nullable = false, length = 500)
    private String message;

    @Column(nullable = false)
    private Boolean isActive = true;

    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt = LocalDateTime.now();

    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt = LocalDateTime.now();
}

