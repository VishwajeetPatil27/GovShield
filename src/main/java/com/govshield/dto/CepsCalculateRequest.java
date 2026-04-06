package com.govshield.dto;

import lombok.Data;

@Data
public class CepsCalculateRequest {
    private Integer vehiclesCount;
    private Double landAcres;
    private Integer electricityUnitsMonthly;
    private Double declaredAssetsValue;
    private String employmentStatus;
}

