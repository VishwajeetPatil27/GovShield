package com.govshield.dto;

import lombok.Data;

@Data
public class ProjectUpdateRequest {
    private Integer reportedProgress;
    private String message;
    private String photoBase64;
    private Double geoLat;
    private Double geoLng;
}

