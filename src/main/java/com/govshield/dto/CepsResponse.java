package com.govshield.dto;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class CepsResponse {
    private String ugid;
    private Integer cepsScore;
    private String cepsCategory;
    private Integer incomeScore;
    private Integer assetScore;
    private Integer landScore;
    private Integer employmentScore;
    private Integer utilityScore;
    private String calculatedAt;
}

