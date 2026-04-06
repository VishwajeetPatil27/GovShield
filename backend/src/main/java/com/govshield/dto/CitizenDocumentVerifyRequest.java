package com.govshield.dto;

import lombok.Data;

@Data
public class CitizenDocumentVerifyRequest {
    private boolean approved;
    private String remarks;
}
