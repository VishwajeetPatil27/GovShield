package com.govshield.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Entity
@Table(name = "citizen_documents")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class CitizenDocument {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "citizen_id", nullable = false)
    private Citizen citizen;

    @Column(nullable = false, length = 100)
    private String documentType;

    @Column(length = 100)
    private String documentNumber;

    @Column(nullable = false, length = 255)
    private String fileName;

    @Lob
    @Column(nullable = false, columnDefinition = "LONGTEXT")
    private String fileContentBase64;

    @Column(nullable = false, length = 20)
    private String verificationStatus; // PENDING, VERIFIED, REJECTED

    @Column(length = 500)
    private String verificationRemarks;

    @Column(length = 20)
    private String verifiedByRole;

    @Column(nullable = false)
    private LocalDateTime uploadedAt;

    private LocalDateTime verifiedAt;
}
