package com.govshield.controller;

import com.govshield.dto.CitizenRegistrationSummary;
import com.govshield.model.Citizen;
import com.govshield.service.CitizenService;
import com.govshield.util.RoleGuard;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.util.List;

@RestController
@RequestMapping("/api/citizens")
@CrossOrigin(origins = "*", maxAge = 3600)
public class CitizenController {

    @Autowired
    private CitizenService citizenService;

    /**
     * Register a new citizen
     */
    @PostMapping("/register")
    public ResponseEntity<Citizen> registerCitizen(@RequestBody Citizen citizen) {
        Citizen registeredCitizen = citizenService.registerCitizen(citizen);
        return ResponseEntity.status(HttpStatus.CREATED).body(registeredCitizen);
    }

    /**
     * Onboard citizen by Aadhaar. Generates UGID only for first-time citizen.
     */
    @PostMapping("/onboard")
    public ResponseEntity<Citizen> onboardCitizen(@RequestBody Citizen citizen) {
        Citizen onboardedCitizen = citizenService.onboardCitizen(citizen);
        return ResponseEntity.status(HttpStatus.CREATED).body(onboardedCitizen);
    }

    /**
     * Get citizen by UGID
     */
    @GetMapping("/ugid/{ugid}")
    public ResponseEntity<Citizen> getCitizenByUgid(@PathVariable String ugid) {
        Citizen citizen = citizenService.getCitizenByUgid(ugid);
        return ResponseEntity.ok(citizen);
    }

    /**
     * Get citizen by ID
     */
    @GetMapping("/{id}")
    public ResponseEntity<Citizen> getCitizenById(@PathVariable Long id) {
        Citizen citizen = citizenService.getCitizenById(id);
        return ResponseEntity.ok(citizen);
    }

    /**
     * Update citizen information
     */
    @PutMapping("/{id}")
    public ResponseEntity<Citizen> updateCitizen(@PathVariable Long id, @RequestBody Citizen citizen) {
        Citizen updatedCitizen = citizenService.updateCitizen(id, citizen);
        return ResponseEntity.ok(updatedCitizen);
    }

    /**
     * Get all citizens
     */
    @GetMapping
    public ResponseEntity<List<Citizen>> getAllCitizens(@RequestHeader(value = "X-User-Role", required = false) String role) {
        if (role != null && !role.isBlank()) {
            RoleGuard.ensureRole(role, "ADMIN", "OFFICER", "AUDITOR");
        }
        List<Citizen> citizens = citizenService.getAllCitizens();
        return ResponseEntity.ok(citizens);
    }

    @GetMapping("/registrations")
    public ResponseEntity<List<CitizenRegistrationSummary>> getRegistrationSummaries(@RequestHeader("X-User-Role") String role) {
        RoleGuard.ensureRole(role, "ADMIN", "OFFICER", "AUDITOR");
        List<CitizenRegistrationSummary> summaries = citizenService.getRegistrationSummaries();
        return ResponseEntity.ok(summaries);
    }

    /**
     * Deactivate citizen
     */
    @DeleteMapping("/{id}")
    public ResponseEntity<String> deactivateCitizen(@PathVariable Long id) {
        citizenService.deactivateCitizen(id);
        return ResponseEntity.ok("Citizen deactivated successfully");
    }
}
