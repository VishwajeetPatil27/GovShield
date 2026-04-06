package com.govshield.controller;

import com.govshield.dto.ProjectAlertResolveRequest;
import com.govshield.dto.ProjectEvidenceRequest;
import com.govshield.dto.ProjectStatusDTO;
import com.govshield.dto.ProjectUpdateRequest;
import com.govshield.model.Project;
import com.govshield.model.ProjectAlert;
import com.govshield.model.ProjectEvidence;
import com.govshield.model.ProjectUpdate;
import com.govshield.service.ProjectMonitoringService;
import com.govshield.service.ProjectTransparencyService;
import com.govshield.util.RoleGuard;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.math.BigDecimal;
import java.util.List;

@RestController
@RequestMapping("/api/projects")
@CrossOrigin(origins = "*", maxAge = 3600)
public class ProjectMonitoringController {

    @Autowired
    private ProjectMonitoringService projectMonitoringService;

    @Autowired
    private ProjectTransparencyService projectTransparencyService;

    /**
     * Create a new project
     */
    @PostMapping
    public ResponseEntity<Project> createProject(@RequestBody Project project,
                                                 @RequestHeader("X-User-Role") String role) {
        RoleGuard.ensureRole(role, "OFFICER", "ADMIN");
        Project createdProject = projectMonitoringService.createProject(project);
        return ResponseEntity.status(HttpStatus.CREATED).body(createdProject);
    }

    /**
     * Get project by ID
     */
    @GetMapping("/{id}")
    public ResponseEntity<Project> getProjectById(@PathVariable Long id) {
        Project project = projectMonitoringService.getProjectById(id);
        return ResponseEntity.ok(project);
    }

    /**
     * Get project by code
     */
    @GetMapping("/code/{projectCode}")
    public ResponseEntity<Project> getProjectByCode(@PathVariable String projectCode) {
        Project project = projectMonitoringService.getProjectByCode(projectCode);
        return ResponseEntity.ok(project);
    }

    /**
     * Get all projects
     */
    @GetMapping
    public ResponseEntity<List<Project>> getAllProjects() {
        List<Project> projects = projectMonitoringService.getAllProjects();
        return ResponseEntity.ok(projects);
    }

    /**
     * Get projects by MLA
     */
    @GetMapping("/mla/{mlaName}")
    public ResponseEntity<List<Project>> getProjectsByMla(@PathVariable String mlaName) {
        List<Project> projects = projectMonitoringService.getProjectsByMla(mlaName);
        return ResponseEntity.ok(projects);
    }

    /**
     * Get projects by MP
     */
    @GetMapping("/mp/{mpName}")
    public ResponseEntity<List<Project>> getProjectsByMp(@PathVariable String mpName) {
        List<Project> projects = projectMonitoringService.getProjectsByMp(mpName);
        return ResponseEntity.ok(projects);
    }

    /**
     * Get projects by status
     */
    @GetMapping("/status/{status}")
    public ResponseEntity<List<Project>> getProjectsByStatus(@PathVariable String status) {
        List<Project> projects = projectMonitoringService.getProjectsByStatus(status);
        return ResponseEntity.ok(projects);
    }

    /**
     * Update project progress
     */
    @PutMapping("/{id}/progress")
    public ResponseEntity<Project> updateProgress(@PathVariable Long id,
                                                  @RequestParam Integer progressPercentage,
                                                  @RequestHeader("X-User-Role") String role) {
        RoleGuard.ensureRole(role, "OFFICER", "ADMIN");
        Project project = projectMonitoringService.updateProgress(id, progressPercentage);
        return ResponseEntity.ok(project);
    }

    /**
     * Update project quality status
     */
    @PutMapping("/{id}/quality")
    public ResponseEntity<Project> updateQualityStatus(@PathVariable Long id,
                                                       @RequestParam String qualityStatus,
                                                       @RequestHeader("X-User-Role") String role) {
        RoleGuard.ensureRole(role, "OFFICER", "ADMIN");
        Project project = projectMonitoringService.updateQualityStatus(id, qualityStatus);
        return ResponseEntity.ok(project);
    }

    /**
     * Release funds for project
     */
    @PostMapping("/{id}/release-funds")
    public ResponseEntity<Project> releaseFunds(@PathVariable Long id,
                                                @RequestParam BigDecimal amount,
                                                @RequestHeader("X-User-Role") String role) {
        RoleGuard.ensureRole(role, "OFFICER", "ADMIN");
        Project project = projectMonitoringService.releaseFunds(id, amount);
        return ResponseEntity.ok(project);
    }

    /**
     * Record expenditure
     */
    @PostMapping("/{id}/expenditure")
    public ResponseEntity<Project> recordExpenditure(@PathVariable Long id,
                                                     @RequestParam BigDecimal amount,
                                                     @RequestHeader("X-User-Role") String role) {
        RoleGuard.ensureRole(role, "OFFICER", "ADMIN");
        Project project = projectMonitoringService.recordExpenditure(id, amount);
        return ResponseEntity.ok(project);
    }

    /**
     * Get project status
     */
    @GetMapping("/{id}/status")
    public ResponseEntity<ProjectStatusDTO> getProjectStatus(@PathVariable Long id) {
        ProjectStatusDTO status = projectMonitoringService.getProjectStatus(id);
        return ResponseEntity.ok(status);
    }

    /**
     * Get all project statuses
     */
    @GetMapping("/status/all")
    public ResponseEntity<List<ProjectStatusDTO>> getAllProjectStatuses() {
        List<ProjectStatusDTO> statuses = projectMonitoringService.getAllProjectStatuses();
        return ResponseEntity.ok(statuses);
    }

    /**
     * Official progress update (Officer/Admin)
     */
    @PostMapping("/{id}/updates")
    public ResponseEntity<ProjectUpdate> addProjectUpdate(@PathVariable Long id,
                                                          @RequestBody ProjectUpdateRequest request,
                                                          @RequestHeader("X-User-Role") String role,
                                                          @RequestHeader(value = "X-User-Id", required = false) String identifier) {
        RoleGuard.ensureRole(role, "OFFICER", "ADMIN");
        ProjectUpdate update = projectTransparencyService.addUpdate(id, request, role, identifier);
        return ResponseEntity.status(HttpStatus.CREATED).body(update);
    }

    /**
     * Citizen evidence submission (Public Corruption Evidence System)
     */
    @PostMapping("/{id}/evidence")
    public ResponseEntity<ProjectEvidence> submitEvidence(@PathVariable Long id,
                                                          @RequestBody ProjectEvidenceRequest request,
                                                          @RequestHeader(value = "X-User-Role", required = false) String role) {
        if (role != null && !role.isBlank()) {
            RoleGuard.ensureRole(role, "CITIZEN", "OFFICER", "AUDITOR", "ADMIN");
        }
        ProjectEvidence evidence = projectTransparencyService.submitEvidence(id, request, role);
        return ResponseEntity.status(HttpStatus.CREATED).body(evidence);
    }

    @GetMapping("/{id}/evidence")
    public ResponseEntity<List<ProjectEvidence>> listEvidence(@PathVariable Long id,
                                                              @RequestHeader(value = "X-User-Role", required = false) String role) {
        if (role != null && !role.isBlank()) {
            RoleGuard.ensureRole(role, "CITIZEN", "OFFICER", "AUDITOR", "ADMIN");
        }
        return ResponseEntity.ok(projectTransparencyService.listEvidenceForProject(id));
    }

    @GetMapping("/alerts")
    public ResponseEntity<List<ProjectAlert>> listAlerts(@RequestHeader("X-User-Role") String role) {
        RoleGuard.ensureRole(role, "OFFICER", "AUDITOR", "ADMIN");
        return ResponseEntity.ok(projectTransparencyService.listActiveAlerts());
    }

    @PostMapping("/alerts/{alertId}/resolve")
    public ResponseEntity<ProjectAlert> resolveAlert(@PathVariable Long alertId,
                                                     @RequestBody(required = false) ProjectAlertResolveRequest request,
                                                     @RequestHeader("X-User-Role") String role,
                                                     @RequestHeader(value = "X-User-Id", required = false) String identifier) {
        RoleGuard.ensureRole(role, "OFFICER", "ADMIN");
        String remarks = request != null ? request.getRemarks() : null;
        return ResponseEntity.ok(projectTransparencyService.resolveAlert(alertId, role, identifier, remarks));
    }

    @PostMapping("/{id}/review/auditor")
    public ResponseEntity<Project> auditorReview(@PathVariable Long id,
                                                 @RequestParam boolean approved,
                                                 @RequestParam(required = false, defaultValue = "") String remarks,
                                                 @RequestHeader("X-User-Role") String role) {
        RoleGuard.ensureRole(role, "AUDITOR");
        Project project = projectMonitoringService.auditorReview(id, approved, remarks);
        return ResponseEntity.ok(project);
    }

    @PostMapping("/{id}/review/officer")
    public ResponseEntity<Project> officerReview(@PathVariable Long id,
                                                 @RequestParam boolean approved,
                                                 @RequestParam(required = false, defaultValue = "") String remarks,
                                                 @RequestHeader("X-User-Role") String role) {
        RoleGuard.ensureRole(role, "OFFICER");
        Project project = projectMonitoringService.officerReview(id, approved, remarks);
        return ResponseEntity.ok(project);
    }

    @PostMapping("/{id}/review/admin")
    public ResponseEntity<Project> adminReview(@PathVariable Long id,
                                               @RequestParam boolean approved,
                                               @RequestParam(required = false, defaultValue = "") String remarks,
                                               @RequestHeader("X-User-Role") String role) {
        RoleGuard.ensureRole(role, "ADMIN");
        Project project = projectMonitoringService.adminReview(id, approved, remarks);
        return ResponseEntity.ok(project);
    }
}
