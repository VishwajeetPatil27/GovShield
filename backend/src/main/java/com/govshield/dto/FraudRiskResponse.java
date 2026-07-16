package com.govshield.dto;

import java.time.LocalDateTime;
import java.util.List;

public class FraudRiskResponse {
    private Long citizenId;
    private String citizenName;
    private String aadhaar;
    private String bankAccountNumber;
    private Integer riskScore;
    private String riskLevel;
    private Boolean duplicateAadhaar;
    private Boolean duplicateBankAccount;
    private Boolean incomeMismatch;
    private Boolean schemeConflict;
    private List<String> reasons;
    private LocalDateTime analyzedAt;

    public FraudRiskResponse() {
    }

    public FraudRiskResponse(Long citizenId, String citizenName, String aadhaar, String bankAccountNumber,
                             Integer riskScore, String riskLevel, Boolean duplicateAadhaar,
                             Boolean duplicateBankAccount, Boolean incomeMismatch, Boolean schemeConflict,
                             List<String> reasons, LocalDateTime analyzedAt) {
        this.citizenId = citizenId;
        this.citizenName = citizenName;
        this.aadhaar = aadhaar;
        this.bankAccountNumber = bankAccountNumber;
        this.riskScore = riskScore;
        this.riskLevel = riskLevel;
        this.duplicateAadhaar = duplicateAadhaar;
        this.duplicateBankAccount = duplicateBankAccount;
        this.incomeMismatch = incomeMismatch;
        this.schemeConflict = schemeConflict;
        this.reasons = reasons;
        this.analyzedAt = analyzedAt;
    }

    public Long getCitizenId() { return citizenId; }
    public String getCitizenName() { return citizenName; }
    public String getAadhaar() { return aadhaar; }
    public String getBankAccountNumber() { return bankAccountNumber; }
    public Integer getRiskScore() { return riskScore; }
    public String getRiskLevel() { return riskLevel; }
    public Boolean getDuplicateAadhaar() { return duplicateAadhaar; }
    public Boolean getDuplicateBankAccount() { return duplicateBankAccount; }
    public Boolean getIncomeMismatch() { return incomeMismatch; }
    public Boolean getSchemeConflict() { return schemeConflict; }
    public List<String> getReasons() { return reasons; }
    public LocalDateTime getAnalyzedAt() { return analyzedAt; }
}
