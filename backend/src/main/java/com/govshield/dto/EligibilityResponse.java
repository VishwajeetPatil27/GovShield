package com.govshield.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class EligibilityResponse {
    private Long enrollmentId;
    private String enrollmentNumber;
    private String eligibilityStatus; // ELIGIBLE, INELIGIBLE, FLAGGED
    private String fraudRiskLevel; // LOW, MEDIUM, HIGH
    private String message;
    private Boolean eligible;
}
