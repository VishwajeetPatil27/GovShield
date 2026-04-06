package com.govshield.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.math.BigDecimal;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ProjectStatusDTO {
    private Long projectId;
    private String projectCode;
    private String projectName;
    private String status;
    private Integer progressPercentage;
    private BigDecimal totalBudget;
    private BigDecimal releasedAmount;
    private BigDecimal spentAmount;
    private String qualityStatus;
}
