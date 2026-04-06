package com.govshield.service;

import com.govshield.dto.CitizenRegistrationSummary;
import com.govshield.exception.CustomException;
import com.govshield.model.Citizen;
import com.govshield.model.CitizenDocument;
import com.govshield.repository.CitizenDocumentRepository;
import com.govshield.repository.CitizenRepository;
import com.govshield.util.UgidGenerator;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import java.time.LocalDate;
import java.util.List;

@Service
public class CitizenService {

    @Autowired
    private CitizenRepository citizenRepository;

    @Autowired
    private CitizenDocumentRepository citizenDocumentRepository;

    /**
     * Register a new citizen
     */
    public Citizen registerCitizen(Citizen citizen) {
        citizen.setAadhaar(normalizeAadhaar(citizen.getAadhaar()));
        citizen.setPan(normalizeOptional(citizen.getPan()));
        citizen.setEmail(normalizeOptional(citizen.getEmail()));
        citizen.setPhoneNumber(normalizeOptional(citizen.getPhoneNumber()));
        // Check if citizen already exists
        if (citizenRepository.findByAadhaar(citizen.getAadhaar()).isPresent()) {
            throw new CustomException("Citizen with this Aadhaar already exists", "DUPLICATE_AADHAAR", 409);
        }

        if (citizenRepository.findByEmail(citizen.getEmail()).isPresent()) {
            throw new CustomException("Citizen with this email already exists", "DUPLICATE_EMAIL", 409);
        }

        // Generate deterministic UGID from Aadhaar; generated once and reused forever.
        citizen.setUgid(UgidGenerator.generateFromAadhaar(citizen.getAadhaar()));
        citizen.setCreatedAt(LocalDate.now());
        citizen.setUpdatedAt(LocalDate.now());
        citizen.setIsActive(true);
        citizen.setVerificationStatus("PENDING");

        return citizenRepository.save(citizen);
    }

    /**
     * Onboard citizen: create on first time, return existing record on repeat.
     */
    public Citizen onboardCitizen(Citizen citizen) {
        citizen.setAadhaar(normalizeAadhaar(citizen.getAadhaar()));
        return citizenRepository.findByAadhaar(citizen.getAadhaar())
            .orElseGet(() -> registerCitizen(citizen));
    }

    /**
     * Get citizen by UGID
     */
    public Citizen getCitizenByUgid(String ugid) {
        return citizenRepository.findByUgid(ugid)
            .orElseThrow(() -> new CustomException("Citizen not found", "CITIZEN_NOT_FOUND", 404));
    }

    /**
     * Get citizen by ID
     */
    public Citizen getCitizenById(Long id) {
        return citizenRepository.findById(id)
            .orElseThrow(() -> new CustomException("Citizen not found", "CITIZEN_NOT_FOUND", 404));
    }

    /**
     * Update citizen information
     */
    public Citizen updateCitizen(Long id, Citizen citizen) {
        Citizen existingCitizen = getCitizenById(id);
        
        existingCitizen.setFirstName(citizen.getFirstName());
        existingCitizen.setLastName(citizen.getLastName());
        existingCitizen.setAddress(citizen.getAddress());
        existingCitizen.setAnnualIncome(citizen.getAnnualIncome());
        existingCitizen.setEmploymentStatus(citizen.getEmploymentStatus());
        existingCitizen.setIsGovernmentEmployee(citizen.getIsGovernmentEmployee());
        existingCitizen.setUpdatedAt(LocalDate.now());

        return citizenRepository.save(existingCitizen);
    }

    /**
     * Get all citizens
     */
    public List<Citizen> getAllCitizens() {
        return citizenRepository.findAll();
    }

    public List<CitizenRegistrationSummary> getRegistrationSummaries() {
        return citizenRepository.findAll().stream().map(citizen -> {
            List<CitizenDocument> docs = citizenDocumentRepository.findByCitizenIdOrderByUploadedAtDesc(citizen.getId());
            long verified = docs.stream().filter(d -> "VERIFIED".equalsIgnoreCase(d.getVerificationStatus())).count();
            long pending = docs.stream().filter(d -> "PENDING".equalsIgnoreCase(d.getVerificationStatus())).count();
            String firstName = citizen.getFirstName() == null ? "" : citizen.getFirstName();
            String lastName = citizen.getLastName() == null ? "" : citizen.getLastName();
            String name = (firstName + " " + lastName).trim();
            return new CitizenRegistrationSummary(
                citizen.getId(),
                citizen.getUgid(),
                name,
                citizen.getEmail(),
                citizen.getPhoneNumber(),
                citizen.getCreatedAt(),
                citizen.getVerificationStatus(),
                docs.size(),
                verified,
                pending
            );
        }).toList();
    }

    /**
     * Deactivate citizen
     */
    public void deactivateCitizen(Long id) {
        Citizen citizen = getCitizenById(id);
        citizen.setIsActive(false);
        citizen.setUpdatedAt(LocalDate.now());
        citizenRepository.save(citizen);
    }

    private String normalizeAadhaar(String aadhaar) {
        if (aadhaar == null) {
            return "";
        }
        return aadhaar.replaceAll("\\s+", "").trim();
    }

    private String normalizeOptional(String value) {
        if (value == null) {
            return null;
        }
        String normalized = value.trim();
        return normalized.isEmpty() ? null : normalized;
    }
}
