package com.govshield.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import java.util.List;

@Data
@AllArgsConstructor
public class RealtimeEligibilityResponse {
    private Integer cepsScore;
    private String cepsCategory;
    private List<RealtimeSchemeResult> results;
}

