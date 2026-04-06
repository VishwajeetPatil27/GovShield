package com.govshield.dto;

import lombok.Data;

@Data
public class RealtimeEligibilityRequest {
    private String ugid; // optional: enables conflict checks + CEPS fetch

    private Integer age; // optional alternative to ugid
    private Long annualIncome; // optional alternative to ugid
    private Boolean isGovernmentEmployee;

    private Integer vehiclesCount;
    private Double landAcres;
    private Integer electricityUnitsMonthly;
    private Double declaredAssetsValue;
    private String employmentStatus;
}

