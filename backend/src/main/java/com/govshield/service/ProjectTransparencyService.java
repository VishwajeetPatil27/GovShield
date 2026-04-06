package com.govshield.service;

import com.govshield.dto.ProjectEvidenceRequest;
import com.govshield.dto.ProjectUpdateRequest;
import com.govshield.exception.CustomException;
import com.govshield.model.*;
import com.govshield.repository.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Locale;
import java.util.Optional;

@Service
public class ProjectTransparencyService {

    @Autowired
    private ProjectRepository projectRepository;

    @Autowired
    private CitizenRepository citizenRepository;

    @Autowired
    private ProjectUpdateRepository projectUpdateRepository;

    @Autowired
    private ProjectEvidenceRepository projectEvidenceRepository;

    @Autowired
    private ProjectAlertRepository projectAlertRepository;

    public ProjectUpdate addUpdate(Long projectId, ProjectUpdateRequest request, String role, String identifier) {
        Project project = projectRepository.findById(projectId)
            .orElseThrow(() -> new CustomException("Project not found", "PROJECT_NOT_FOUND", 404));

        int progress = request.getReportedProgress() == null ? 0 : request.getReportedProgress();
        if (progress < 0 || progress > 100) {
            throw new CustomException("Progress must be between 0 and 100", "INVALID_PROGRESS", 400);
        }

        ProjectUpdate update = new ProjectUpdate();
        update.setProject(project);
        update.setSubmittedByRole(role);
        update.setSubmittedByIdentifier(identifier);
        update.setReportedProgress(progress);
        update.setMessage(request.getMessage());
        update.setPhotoBase64(request.getPhotoBase64());
        update.setGeoLat(request.getGeoLat());
        update.setGeoLng(request.getGeoLng());
        update.setCreatedAt(LocalDateTime.now());

        ProjectUpdate saved = projectUpdateRepository.save(update);

        // If the public has reported a very different progress recently, raise an alert.
        maybeCreateAlertsFromUpdate(project, saved);
        return saved;
    }

    public ProjectEvidence submitEvidence(Long projectId, ProjectEvidenceRequest request, String roleHeader) {
        Project project = projectRepository.findById(projectId)
            .orElseThrow(() -> new CustomException("Project not found", "PROJECT_NOT_FOUND", 404));

        Citizen citizen = null;
        if (request.getUgid() != null && !request.getUgid().isBlank()) {
            citizen = citizenRepository.findByUgid(request.getUgid()).orElse(null);
        }

        String evidenceType = normalizeType(request.getEvidenceType());
        if (evidenceType.isBlank()) {
            throw new CustomException("evidenceType is required", "INVALID_EVIDENCE_TYPE", 400);
        }

        ProjectEvidence evidence = new ProjectEvidence();
        evidence.setProject(project);
        evidence.setCitizen(citizen);
        evidence.setEvidenceType(evidenceType);
        evidence.setMessage(request.getMessage());
        evidence.setPhotoBase64(request.getPhotoBase64());
        evidence.setGeoLat(request.getGeoLat());
        evidence.setGeoLng(request.getGeoLng());
        evidence.setProgressEstimate(request.getProgressEstimate());
        evidence.setContractorRating(request.getContractorRating());
        evidence.setStatus("NEW");
        evidence.setCreatedAt(LocalDateTime.now());

        ProjectEvidence saved = projectEvidenceRepository.save(evidence);
        maybeCreateAlertsFromEvidence(project, saved);
        return saved;
    }

    public List<ProjectEvidence> listEvidenceForProject(Long projectId) {
        return projectEvidenceRepository.findByProjectIdOrderByCreatedAtDesc(projectId);
    }

    public List<ProjectAlert> listActiveAlerts() {
        return projectAlertRepository.findByResolvedFalseOrderByCreatedAtDesc();
    }

    public ProjectAlert resolveAlert(Long alertId, String role, String identifier, String remarks) {
        ProjectAlert alert = projectAlertRepository.findById(alertId)
            .orElseThrow(() -> new CustomException("Alert not found", "ALERT_NOT_FOUND", 404));
        alert.setResolved(true);
        alert.setResolvedByRole(role);
        alert.setResolvedByIdentifier(identifier);
        alert.setResolvedAt(LocalDateTime.now());
        if (remarks != null && !remarks.isBlank()) {
            String updatedReason = alert.getReason() + " | Resolved: " + remarks;
            alert.setReason(updatedReason.length() > 500 ? updatedReason.substring(0, 500) : updatedReason);
        }
        return projectAlertRepository.save(alert);
    }

    private void maybeCreateAlertsFromEvidence(Project project, ProjectEvidence evidence) {
        Optional<ProjectUpdate> latestUpdate = projectUpdateRepository.findByProjectIdOrderByCreatedAtDesc(project.getId())
            .stream().findFirst();

        if (evidence.getProgressEstimate() != null && latestUpdate.isPresent()) {
            int diff = Math.abs(latestUpdate.get().getReportedProgress() - evidence.getProgressEstimate());
            if (diff >= 40) {
                createAlert(project, evidence, latestUpdate.get(), "HIGH",
                    "Progress discrepancy: reported " + latestUpdate.get().getReportedProgress() +
                        "% vs citizen estimate " + evidence.getProgressEstimate() + "%");
            } else if (diff >= 25) {
                createAlert(project, evidence, latestUpdate.get(), "MEDIUM",
                    "Progress discrepancy: reported " + latestUpdate.get().getReportedProgress() +
                        "% vs citizen estimate " + evidence.getProgressEstimate() + "%");
            }
        }

        if (evidence.getContractorRating() != null && evidence.getContractorRating() <= 2) {
            createAlert(project, evidence, latestUpdate.orElse(null), "MEDIUM",
                "Low contractor rating reported by citizen (" + evidence.getContractorRating() + "/5)");
        }

        if ("COMPLAINT".equalsIgnoreCase(evidence.getEvidenceType())) {
            String msg = (evidence.getMessage() == null ? "" : evidence.getMessage()).toLowerCase(Locale.ROOT);
            if (msg.contains("bribe") || msg.contains("fraud") || msg.contains("ghost") || msg.contains("fake")) {
                createAlert(project, evidence, latestUpdate.orElse(null), "HIGH",
                    "Citizen complaint indicates possible corruption: keyword match");
            } else if (msg.contains("poor") || msg.contains("delay") || msg.contains("quality")) {
                createAlert(project, evidence, latestUpdate.orElse(null), "LOW",
                    "Citizen complaint indicates quality/delay concerns");
            }
        }
    }

    private void maybeCreateAlertsFromUpdate(Project project, ProjectUpdate update) {
        List<ProjectEvidence> recentEvidence = projectEvidenceRepository.findByProjectIdOrderByCreatedAtDesc(project.getId());
        if (recentEvidence.isEmpty()) return;

        ProjectEvidence latestEvidence = recentEvidence.get(0);
        if (latestEvidence.getProgressEstimate() == null) return;

        int diff = Math.abs(update.getReportedProgress() - latestEvidence.getProgressEstimate());
        if (diff >= 40) {
            createAlert(project, latestEvidence, update, "HIGH",
                "Progress discrepancy after new update: reported " + update.getReportedProgress() +
                    "% vs citizen estimate " + latestEvidence.getProgressEstimate() + "%");
        }
    }

    private void createAlert(Project project, ProjectEvidence evidence, ProjectUpdate update, String severity, String reason) {
        ProjectAlert alert = new ProjectAlert();
        alert.setProject(project);
        alert.setEvidence(evidence);
        alert.setUpdate(update);
        alert.setSeverity(severity);
        alert.setReason(reason.length() > 500 ? reason.substring(0, 500) : reason);
        alert.setResolved(false);
        alert.setCreatedAt(LocalDateTime.now());
        projectAlertRepository.save(alert);
    }

    private String normalizeType(String value) {
        return value == null ? "" : value.trim().toUpperCase(Locale.ROOT);
    }
}

