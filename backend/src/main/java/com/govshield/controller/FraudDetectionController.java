package com.govshield.controller;

import com.govshield.model.Enrollment;
import com.govshield.service.FraudDetectionService;
import com.govshield.util.RoleGuard;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.util.List;

@RestController
@RequestMapping("/api/fraud")
@CrossOrigin(origins = "*", maxAge = 3600)
public class FraudDetectionController {

    @Autowired
    private FraudDetectionService fraudDetectionService;

    /**
     * Detect fraud patterns for a citizen
     */
    @GetMapping("/detect/{citizenId}")
    public ResponseEntity<List<Enrollment>> detectFraudPatterns(@PathVariable Long citizenId) {
        List<Enrollment> fraudEnrollments = fraudDetectionService.detectFraudPatterns(citizenId);
        return ResponseEntity.ok(fraudEnrollments);
    }

    /**
     * Flag enrollment as fraud
     */
    @PostMapping("/flag/{enrollmentId}")
    public ResponseEntity<Enrollment> flagAsFraud(@PathVariable Long enrollmentId, 
                                                  @RequestParam String reason,
                                                  @RequestHeader("X-User-Role") String role) {
        RoleGuard.ensureRole(role, "AUDITOR", "ADMIN");
        Enrollment enrollment = fraudDetectionService.flagAsfraud(enrollmentId, reason);
        return ResponseEntity.ok(enrollment);
    }

    /**
     * Detect all frauds
     */
    @GetMapping("/detect-all")
    public ResponseEntity<List<Enrollment>> detectAllFrauds(@RequestHeader("X-User-Role") String role) {
        RoleGuard.ensureRole(role, "AUDITOR", "ADMIN");
        List<Enrollment> fraudEnrollments = fraudDetectionService.detectAllFrauds();
        return ResponseEntity.ok(fraudEnrollments);
    }

    /**
     * Get fraud alerts
     */
    @GetMapping("/alerts")
    public ResponseEntity<List<Enrollment>> getFraudAlerts() {
        List<Enrollment> alerts = fraudDetectionService.getFraudAlerts();
        return ResponseEntity.ok(alerts);
    }
}
