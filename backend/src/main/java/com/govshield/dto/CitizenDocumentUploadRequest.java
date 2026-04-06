package com.govshield.dto;

import lombok.Data;

@Data
public class CitizenDocumentUploadRequest {
    private Long citizenId;
    private String documentType;
    private String documentNumber;
    private String fileName;
    private String fileContentBase64;
}
