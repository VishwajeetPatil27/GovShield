package com.govshield.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.time.LocalDateTime;

@Entity
@Table(name = "citizen_economic_profiles")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class CitizenEconomicProfile {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @OneToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "citizen_id", nullable = false, unique = true)
    private Citizen citizen;

    @Column(nullable = false)
    private Integer incomeScore = 0;

    @Column(nullable = false)
    private Integer assetScore = 0;

    @Column(nullable = false)
    private Integer landScore = 0;

    @Column(nullable = false)
    private Integer employmentScore = 0;

    @Column(nullable = false)
    private Integer utilityScore = 0;

    @Column(nullable = false)
    private Integer cepsScore = 0;

    @Column(nullable = false, length = 30)
    private String cepsCategory = "UNKNOWN";

    @Column(columnDefinition = "TEXT")
    private String factorsJson;

    @Column(name = "calculated_at", nullable = false)
    private LocalDateTime calculatedAt = LocalDateTime.now();
}

