package com.govshield.service;

import com.govshield.exception.CustomException;
import com.govshield.model.Scheme;
import com.govshield.repository.SchemeRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import java.time.LocalDateTime;
import java.util.List;

@Service
public class SchemeService {

    @Autowired
    private SchemeRepository schemeRepository;

    /**
     * Create a new scheme
     */
    public Scheme createScheme(Scheme scheme) {
        if (schemeRepository.findBySchemeCode(scheme.getSchemeCode()).isPresent()) {
            throw new CustomException("Scheme with this code already exists", "DUPLICATE_SCHEME_CODE", 409);
        }

        scheme.setCreatedAt(LocalDateTime.now());
        scheme.setUpdatedAt(LocalDateTime.now());
        scheme.setIsActive(true);
        if (scheme.getUsesCeps() == null) scheme.setUsesCeps(false);
        if (scheme.getMinCepsScore() == null) scheme.setMinCepsScore(0);
        if (scheme.getMaxCepsScore() == null) scheme.setMaxCepsScore(100);

        return schemeRepository.save(scheme);
    }

    /**
     * Get scheme by ID
     */
    public Scheme getSchemeById(Long id) {
        return schemeRepository.findById(id)
            .orElseThrow(() -> new CustomException("Scheme not found", "SCHEME_NOT_FOUND", 404));
    }

    /**
     * Get scheme by code
     */
    public Scheme getSchemeByCode(String schemeCode) {
        return schemeRepository.findBySchemeCode(schemeCode)
            .orElseThrow(() -> new CustomException("Scheme not found", "SCHEME_NOT_FOUND", 404));
    }

    /**
     * Get all active schemes
     */
    public List<Scheme> getAllActiveSchemes() {
        return schemeRepository.findByIsActive(true);
    }

    /**
     * Get schemes by sector
     */
    public List<Scheme> getSchemesBySector(String sector) {
        return schemeRepository.findBySector(sector);
    }

    /**
     * Update scheme
     */
    public Scheme updateScheme(Long id, Scheme scheme) {
        Scheme existingScheme = getSchemeById(id);

        existingScheme.setSchemeName(scheme.getSchemeName());
        existingScheme.setDescription(scheme.getDescription());
        existingScheme.setBenefitAmount(scheme.getBenefitAmount());
        existingScheme.setMaxAnnualIncome(scheme.getMaxAnnualIncome());
        existingScheme.setMaxAge(scheme.getMaxAge());
        existingScheme.setMinAge(scheme.getMinAge());
        existingScheme.setIsGovernmentEmployeeEligible(scheme.getIsGovernmentEmployeeEligible());
        existingScheme.setUsesCeps(scheme.getUsesCeps() != null ? scheme.getUsesCeps() : existingScheme.getUsesCeps());
        existingScheme.setMinCepsScore(scheme.getMinCepsScore() != null ? scheme.getMinCepsScore() : existingScheme.getMinCepsScore());
        existingScheme.setMaxCepsScore(scheme.getMaxCepsScore() != null ? scheme.getMaxCepsScore() : existingScheme.getMaxCepsScore());
        existingScheme.setUpdatedAt(LocalDateTime.now());

        return schemeRepository.save(existingScheme);
    }

    /**
     * Deactivate scheme
     */
    public void deactivateScheme(Long id) {
        Scheme scheme = getSchemeById(id);
        scheme.setIsActive(false);
        scheme.setUpdatedAt(LocalDateTime.now());
        schemeRepository.save(scheme);
    }
}
