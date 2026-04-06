package com.govshield.dto;

import lombok.AllArgsConstructor;
import lombok.Data;

import java.time.LocalDate;

@Data
@AllArgsConstructor
public class CitizenRegistrationSummary {
    private Long citizenId;
    private String ugid;
    private String name;
    private String email;
    private String phoneNumber;
    private LocalDate registeredDate;
    private String verificationStatus;
    private long totalDocuments;
    private long verifiedDocuments;
    private long pendingDocuments;
}
