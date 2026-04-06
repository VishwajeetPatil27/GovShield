package com.govshield.dto;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class RealtimeSchemeResult {
    private Long schemeId;
    private String schemeCode;
    private String schemeName;
    private String sector;
    private Boolean eligible;
    private String reason;
}

