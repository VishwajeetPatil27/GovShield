package com.govshield.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.time.LocalDate;

@Entity
@Table(name = "citizens")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Citizen {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(unique = true, nullable = false)
    private String ugid;

    @Column(unique = true, nullable = false)
    private String aadhaar;

    @Column(unique = true)
    private String pan;

    @Column(nullable = false)
    private String firstName;

    @Column(nullable = false)
    private String lastName;

    @Column(unique = true, nullable = false)
    private String email;

    @Column(unique = true)
    private String phoneNumber;

    @Column(nullable = false)
    private LocalDate dateOfBirth;

    @Column(nullable = false)
    private String gender;

    @Column(nullable = false)
    private String address;

    @Column(nullable = false)
    private String state;

    @Column(nullable = false)
    private String district;

    @Column(nullable = false)
    private String pincode;

    @Column(nullable = false)
    private Double annualIncome;

    @Column(nullable = false)
    private String employmentStatus; // EMPLOYED, UNEMPLOYED, SELF_EMPLOYED

    @Column(nullable = false)
    private Boolean isGovernmentEmployee; // true if employed by government

    @Column(nullable = false)
    private Boolean isBelowPovertyLine;

    @Column(nullable = false)
    private Boolean isActive;

    @Column(nullable = false)
    private LocalDate createdAt;

    @Column(nullable = false)
    private LocalDate updatedAt;

    @Column(length = 500)
    private String remarks;

    @Column(length = 20, nullable = false, columnDefinition = "VARCHAR(20) DEFAULT 'PENDING'")
    private String verificationStatus = "PENDING";
}
