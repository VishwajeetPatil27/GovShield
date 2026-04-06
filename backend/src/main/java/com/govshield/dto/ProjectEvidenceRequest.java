package com.govshield.dto;

import lombok.Data;

@Data
public class ProjectEvidenceRequest {
    private String ugid; // optional; helps link evidence to citizen
    private String evidenceType; // PHOTO|COMPLAINT|REVIEW
    private String message;
    private String photoBase64;
    private Double geoLat;
    private Double geoLng;
    private Integer progressEstimate;
    private Integer contractorRating;
}

