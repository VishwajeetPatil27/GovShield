package com.govshield.controller;

import com.govshield.dto.EligibilityResponse;
import com.govshield.dto.RealtimeEligibilityRequest;
import com.govshield.dto.RealtimeEligibilityResponse;
import com.govshield.dto.SchemeApplyRequest;
import com.govshield.model.Enrollment;
import com.govshield.service.EligibilityService;
import com.govshield.util.RoleGuard;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.util.List;

@RestController
@RequestMapping("/api/eligibility")
@CrossOrigin(origins = "*", maxAge = 3600)
public class EligibilityController {

    @Autowired
    private EligibilityService eligibilityService;

    /**
     * Apply for a scheme
     */
    @PostMapping("/apply")
    public ResponseEntity<EligibilityResponse> applyForScheme(@RequestBody SchemeApplyRequest request,
                                                              @RequestHeader(value = "X-User-Role", required = false) String role) {
        if (role != null && !role.isBlank()) {
            RoleGuard.ensureRole(role, "CITIZEN");
        }
        EligibilityResponse response = eligibilityService.applyForScheme(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    /**
     * Check eligibility without applying
     */
    @PostMapping("/check")
    public ResponseEntity<EligibilityResponse> checkEligibility(@RequestParam String ugid, @RequestParam Long schemeId) {
        EligibilityResponse response = eligibilityService.checkEligibility(ugid, schemeId);
        return ResponseEntity.ok(response);
    }

    @PostMapping("/realtime-check")
    public ResponseEntity<RealtimeEligibilityResponse> realtimeCheck(@RequestBody RealtimeEligibilityRequest request) {
        return ResponseEntity.ok(eligibilityService.realtimeCheck(request));
    }

    /**
     * Get enrollment by ID
     */
    @GetMapping("/enrollment/{id}")
    public ResponseEntity<Enrollment> getEnrollmentById(@PathVariable Long id) {
        Enrollment enrollment = eligibilityService.getEnrollmentById(id);
        return ResponseEntity.ok(enrollment);
    }

    /**
     * Get all enrollments for citizen
     */
    @GetMapping("/citizen/{citizenId}")
    public ResponseEntity<List<Enrollment>> getCitizenEnrollments(@PathVariable Long citizenId) {
        List<Enrollment> enrollments = eligibilityService.getCitizenEnrollments(citizenId);
        return ResponseEntity.ok(enrollments);
    }

    /**
     * Get all enrollments
     */
    @GetMapping("/all")
    public ResponseEntity<List<Enrollment>> getAllEnrollments(@RequestHeader("X-User-Role") String role) {
        RoleGuard.ensureRole(role, "AUDITOR", "OFFICER", "ADMIN");
        List<Enrollment> enrollments = eligibilityService.getAllEnrollments();
        return ResponseEntity.ok(enrollments);
    }

    /**
     * Approve enrollment
     */
    @PostMapping("/approve/{enrollmentId}")
    public ResponseEntity<Enrollment> approveEnrollment(@PathVariable Long enrollmentId) {
        Enrollment enrollment = eligibilityService.approveEnrollment(enrollmentId);
        return ResponseEntity.ok(enrollment);
    }

    /**
     * Reject enrollment
     */
    @PostMapping("/reject/{enrollmentId}")
    public ResponseEntity<Enrollment> rejectEnrollment(@PathVariable Long enrollmentId, 
                                                       @RequestParam String reason) {
        Enrollment enrollment = eligibilityService.rejectEnrollment(enrollmentId, reason);
        return ResponseEntity.ok(enrollment);
    }

    @PostMapping("/review/auditor/{enrollmentId}")
    public ResponseEntity<Enrollment> auditorReview(@PathVariable Long enrollmentId,
                                                    @RequestParam boolean approved,
                                                    @RequestParam(required = false, defaultValue = "") String remarks,
                                                    @RequestHeader("X-User-Role") String role) {
        RoleGuard.ensureRole(role, "AUDITOR");
        Enrollment enrollment = eligibilityService.auditorReview(enrollmentId, approved, remarks);
        return ResponseEntity.ok(enrollment);
    }

    @PostMapping("/review/officer/{enrollmentId}")
    public ResponseEntity<Enrollment> officerReview(@PathVariable Long enrollmentId,
                                                    @RequestParam boolean approved,
                                                    @RequestParam(required = false, defaultValue = "") String remarks,
                                                    @RequestHeader("X-User-Role") String role) {
        RoleGuard.ensureRole(role, "OFFICER");
        Enrollment enrollment = eligibilityService.officerReview(enrollmentId, approved, remarks);
        return ResponseEntity.ok(enrollment);
    }

    @PostMapping("/review/admin/{enrollmentId}")
    public ResponseEntity<Enrollment> adminReview(@PathVariable Long enrollmentId,
                                                  @RequestParam boolean approved,
                                                  @RequestParam(required = false, defaultValue = "") String remarks,
                                                  @RequestHeader("X-User-Role") String role) {
        RoleGuard.ensureRole(role, "ADMIN");
        Enrollment enrollment = eligibilityService.adminReview(enrollmentId, approved, remarks);
        return ResponseEntity.ok(enrollment);
    }
}
