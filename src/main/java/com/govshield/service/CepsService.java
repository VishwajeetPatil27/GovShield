package com.govshield.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.govshield.dto.CepsCalculateRequest;
import com.govshield.dto.CepsResponse;
import com.govshield.exception.CustomException;
import com.govshield.model.Citizen;
import com.govshield.model.CitizenEconomicProfile;
import com.govshield.repository.CitizenEconomicProfileRepository;
import com.govshield.repository.CitizenRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import java.time.LocalDateTime;
import java.util.LinkedHashMap;
import java.util.Map;

@Service
public class CepsService {

    @Autowired
    private CitizenRepository citizenRepository;

    @Autowired
    private CitizenEconomicProfileRepository economicProfileRepository;

    private final ObjectMapper objectMapper = new ObjectMapper();

    public CepsResponse getByUgid(String ugid) {
        Citizen citizen = citizenRepository.findByUgid(ugid)
            .orElseThrow(() -> new CustomException("Citizen not found", "CITIZEN_NOT_FOUND", 404));

        CitizenEconomicProfile profile = economicProfileRepository.findByCitizenId(citizen.getId())
            .orElseThrow(() -> new CustomException("CEPS profile not found", "CEPS_NOT_FOUND", 404));

        return toResponse(citizen, profile);
    }

    public CepsResponse calculateAndUpsert(String ugid, CepsCalculateRequest request) {
        Citizen citizen = citizenRepository.findByUgid(ugid)
            .orElseThrow(() -> new CustomException("Citizen not found", "CITIZEN_NOT_FOUND", 404));

        int incomeScore = scoreIncome(citizen.getAnnualIncome());
        int assetScore = scoreAssets(request);
        int landScore = scoreLand(request);
        int employmentScore = scoreEmployment(request, citizen);
        int utilityScore = scoreUtility(request);

        int total = incomeScore + assetScore + landScore + employmentScore + utilityScore;
        if (total > 100) total = 100;
        if (total < 0) total = 0;

        String category = categorize(total);

        CitizenEconomicProfile profile = economicProfileRepository.findByCitizenId(citizen.getId())
            .orElseGet(CitizenEconomicProfile::new);
        profile.setCitizen(citizen);
        profile.setIncomeScore(incomeScore);
        profile.setAssetScore(assetScore);
        profile.setLandScore(landScore);
        profile.setEmploymentScore(employmentScore);
        profile.setUtilityScore(utilityScore);
        profile.setCepsScore(total);
        profile.setCepsCategory(category);
        profile.setFactorsJson(toFactorsJson(request));
        profile.setCalculatedAt(LocalDateTime.now());

        CitizenEconomicProfile saved = economicProfileRepository.save(profile);
        return toResponse(citizen, saved);
    }

    private CepsResponse toResponse(Citizen citizen, CitizenEconomicProfile profile) {
        return new CepsResponse(
            citizen.getUgid(),
            profile.getCepsScore(),
            profile.getCepsCategory(),
            profile.getIncomeScore(),
            profile.getAssetScore(),
            profile.getLandScore(),
            profile.getEmploymentScore(),
            profile.getUtilityScore(),
            profile.getCalculatedAt() != null ? profile.getCalculatedAt().toString() : null
        );
    }

    private int scoreIncome(Double annualIncome) {
        double income = annualIncome == null ? 0 : annualIncome;
        if (income <= 100000) return 3;
        if (income <= 300000) return 10;
        if (income <= 600000) return 16;
        if (income <= 1200000) return 22;
        return 25;
    }

    private int scoreAssets(CepsCalculateRequest request) {
        int vehicles = request.getVehiclesCount() == null ? 0 : Math.max(0, request.getVehiclesCount());
        double assetsValue = request.getDeclaredAssetsValue() == null ? 0 : Math.max(0, request.getDeclaredAssetsValue());
        int vehicleScore = Math.min(15, vehicles * 4);
        int assetsScore = (int) Math.min(10, (assetsValue / 500000.0) * 5); // ~5L steps
        return Math.min(25, vehicleScore + assetsScore);
    }

    private int scoreLand(CepsCalculateRequest request) {
        double land = request.getLandAcres() == null ? 0 : Math.max(0, request.getLandAcres());
        if (land == 0) return 0;
        if (land <= 1) return 5;
        if (land <= 3) return 12;
        if (land <= 6) return 17;
        return 20;
    }

    private int scoreEmployment(CepsCalculateRequest request, Citizen citizen) {
        String status = request.getEmploymentStatus();
        if (status == null || status.isBlank()) status = citizen.getEmploymentStatus();
        status = status == null ? "" : status.trim().toUpperCase();

        if (Boolean.TRUE.equals(citizen.getIsGovernmentEmployee())) return 15;
        if (status.contains("UNEMPLOY")) return 0;
        if (status.contains("SELF")) return 6;
        if (status.contains("EMPLOY")) return 10;
        return 5;
    }

    private int scoreUtility(CepsCalculateRequest request) {
        int units = request.getElectricityUnitsMonthly() == null ? 0 : Math.max(0, request.getElectricityUnitsMonthly());
        if (units <= 100) return 2;
        if (units <= 200) return 6;
        if (units <= 350) return 10;
        return 15;
    }

    private String categorize(int total) {
        if (total <= 30) return "POOR_ELIGIBLE";
        if (total <= 60) return "MODERATE_SUPPORT";
        return "NOT_ELIGIBLE";
    }

    private String toFactorsJson(CepsCalculateRequest request) {
        Map<String, Object> factors = new LinkedHashMap<>();
        factors.put("vehiclesCount", request.getVehiclesCount());
        factors.put("landAcres", request.getLandAcres());
        factors.put("electricityUnitsMonthly", request.getElectricityUnitsMonthly());
        factors.put("declaredAssetsValue", request.getDeclaredAssetsValue());
        factors.put("employmentStatus", request.getEmploymentStatus());
        try {
            return objectMapper.writeValueAsString(factors);
        } catch (JsonProcessingException e) {
            return null;
        }
    }
}

