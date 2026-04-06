package com.govshield.dto;

import lombok.AllArgsConstructor;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@AllArgsConstructor
public class CitizenDocumentResponse {
    private Long id;
    private Long citizenId;
    private String citizenUgid;
    private String citizenName;
    private String documentType;
    private String documentNumber;
    private String fileName;
    private String fileContentBase64;
    private String verificationStatus;
    private String verificationRemarks;
    private String verifiedByRole;
    private LocalDateTime uploadedAt;
    private LocalDateTime verifiedAt;
}
