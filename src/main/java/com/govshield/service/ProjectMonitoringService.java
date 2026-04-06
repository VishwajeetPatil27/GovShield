package com.govshield.service;

import com.govshield.dto.ProjectStatusDTO;
import com.govshield.exception.CustomException;
import com.govshield.model.Project;
import com.govshield.repository.ProjectRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class ProjectMonitoringService {

    @Autowired
    private ProjectRepository projectRepository;

    /**
     * Create a new project
     */
    public Project createProject(Project project) {
        if (projectRepository.findByProjectCode(project.getProjectCode()).isPresent()) {
            throw new CustomException("Project with this code already exists", "DUPLICATE_PROJECT_CODE", 409);
        }

        project.setCreatedAt(LocalDate.now());
        project.setUpdatedAt(LocalDate.now());
        project.setProgressPercentage(0);
        project.setStatus("SUBMITTED");
        project.setCurrentStage("AUDITOR_REVIEW");
        if (project.getReleasedAmount() == null) {
            project.setReleasedAmount(BigDecimal.ZERO);
        }
        if (project.getSpentAmount() == null) {
            project.setSpentAmount(BigDecimal.ZERO);
        }

        return projectRepository.save(project);
    }

    /**
     * Get project by ID
     */
    public Project getProjectById(Long id) {
        return projectRepository.findById(id)
            .orElseThrow(() -> new CustomException("Project not found", "PROJECT_NOT_FOUND", 404));
    }

    /**
     * Get project by code
     */
    public Project getProjectByCode(String projectCode) {
        return projectRepository.findByProjectCode(projectCode)
            .orElseThrow(() -> new CustomException("Project not found", "PROJECT_NOT_FOUND", 404));
    }

    /**
     * Get all projects
     */
    public List<Project> getAllProjects() {
        return projectRepository.findAll();
    }

    /**
     * Get projects by MLA
     */
    public List<Project> getProjectsByMla(String mlaName) {
        return projectRepository.findByAllocatedMla(mlaName);
    }

    /**
     * Get projects by MP
     */
    public List<Project> getProjectsByMp(String mpName) {
        return projectRepository.findByAllocatedMp(mpName);
    }

    /**
     * Get projects by status
     */
    public List<Project> getProjectsByStatus(String status) {
        return projectRepository.findByStatus(status);
    }

    /**
     * Update project progress
     */
    public Project updateProgress(Long id, Integer progressPercentage) {
        Project project = getProjectById(id);
        if (!"APPROVED".equals(project.getStatus()) && !"ONGOING".equals(project.getStatus())) {
            throw new CustomException("Project must be approved before progress updates", "PROJECT_NOT_APPROVED", 400);
        }
        
        if (progressPercentage < 0 || progressPercentage > 100) {
            throw new CustomException("Progress must be between 0 and 100", "INVALID_PROGRESS", 400);
        }

        project.setProgressPercentage(progressPercentage);
        if (progressPercentage == 100) {
            project.setStatus("COMPLETED");
            project.setEndDate(LocalDate.now());
        }
        project.setUpdatedAt(LocalDate.now());

        return projectRepository.save(project);
    }

    /**
     * Update project quality status
     */
    public Project updateQualityStatus(Long id, String qualityStatus) {
        Project project = getProjectById(id);
        
        if (!qualityStatus.matches("GOOD|AVERAGE|POOR")) {
            throw new CustomException("Invalid quality status", "INVALID_QUALITY_STATUS", 400);
        }

        project.setQualityStatus(qualityStatus);
        project.setUpdatedAt(LocalDate.now());

        return projectRepository.save(project);
    }

    /**
     * Release fund for project
     */
    public Project releaseFunds(Long id, java.math.BigDecimal amount) {
        Project project = getProjectById(id);
        if (!"APPROVED".equals(project.getStatus()) && !"ONGOING".equals(project.getStatus())) {
            throw new CustomException("Project must be approved before releasing funds", "PROJECT_NOT_APPROVED", 400);
        }

        if (project.getReleasedAmount().add(amount).compareTo(project.getTotalBudget()) > 0) {
            throw new CustomException("Release amount exceeds total budget", "EXCEEDS_BUDGET", 400);
        }

        project.setReleasedAmount(project.getReleasedAmount().add(amount));
        project.setUpdatedAt(LocalDate.now());

        return projectRepository.save(project);
    }

    /**
     * Record expenditure for project
     */
    public Project recordExpenditure(Long id, java.math.BigDecimal amount) {
        Project project = getProjectById(id);
        if (!"APPROVED".equals(project.getStatus()) && !"ONGOING".equals(project.getStatus())) {
            throw new CustomException("Project must be approved before recording expenditure", "PROJECT_NOT_APPROVED", 400);
        }

        if (project.getSpentAmount().add(amount).compareTo(project.getReleasedAmount()) > 0) {
            throw new CustomException("Expenditure exceeds released amount", "EXCEEDS_RELEASED_AMOUNT", 400);
        }

        project.setSpentAmount(project.getSpentAmount().add(amount));
        project.setUpdatedAt(LocalDate.now());

        return projectRepository.save(project);
    }

    public Project auditorReview(Long id, boolean approved, String remarks) {
        Project project = getProjectById(id);
        if (!"AUDITOR_REVIEW".equals(project.getCurrentStage())) {
            throw new CustomException("Project is not in auditor stage", "INVALID_STAGE", 400);
        }

        project.setAuditorDecision(approved ? "APPROVED" : "REJECTED");
        if (approved) {
            project.setStatus("AUDITOR_APPROVED");
            project.setCurrentStage("OFFICER_REVIEW");
        } else {
            project.setStatus("AUDITOR_REJECTED");
            project.setCurrentStage("CLOSED");
            project.setRemarks(remarks);
        }
        project.setUpdatedAt(LocalDate.now());
        return projectRepository.save(project);
    }

    public Project officerReview(Long id, boolean approved, String remarks) {
        Project project = getProjectById(id);
        if (!"OFFICER_REVIEW".equals(project.getCurrentStage())) {
            throw new CustomException("Project is not in officer stage", "INVALID_STAGE", 400);
        }

        project.setOfficerDecision(approved ? "APPROVED" : "REJECTED");
        if (approved) {
            project.setStatus("OFFICER_APPROVED");
            project.setCurrentStage("ADMIN_REVIEW");
        } else {
            project.setStatus("OFFICER_REJECTED");
            project.setCurrentStage("CLOSED");
            project.setRemarks(remarks);
        }
        project.setUpdatedAt(LocalDate.now());
        return projectRepository.save(project);
    }

    public Project adminReview(Long id, boolean approved, String remarks) {
        Project project = getProjectById(id);
        if (!"ADMIN_REVIEW".equals(project.getCurrentStage())) {
            throw new CustomException("Project is not in admin stage", "INVALID_STAGE", 400);
        }

        project.setAdminDecision(approved ? "APPROVED" : "REJECTED");
        if (approved) {
            project.setStatus("APPROVED");
            project.setCurrentStage("CLOSED");
        } else {
            project.setStatus("REJECTED");
            project.setCurrentStage("CLOSED");
            project.setRemarks(remarks);
        }
        project.setUpdatedAt(LocalDate.now());
        return projectRepository.save(project);
    }

    /**
     * Get project status as DTO
     */
    public ProjectStatusDTO getProjectStatus(Long id) {
        Project project = getProjectById(id);

        return new ProjectStatusDTO(
            project.getId(),
            project.getProjectCode(),
            project.getProjectName(),
            project.getStatus(),
            project.getProgressPercentage(),
            project.getTotalBudget(),
            project.getReleasedAmount(),
            project.getSpentAmount(),
            project.getQualityStatus()
        );
    }

    /**
     * Get all project statuses
     */
    public List<ProjectStatusDTO> getAllProjectStatuses() {
        return projectRepository.findAll().stream()
            .map(project -> new ProjectStatusDTO(
                project.getId(),
                project.getProjectCode(),
                project.getProjectName(),
                project.getStatus(),
                project.getProgressPercentage(),
                project.getTotalBudget(),
                project.getReleasedAmount(),
                project.getSpentAmount(),
                project.getQualityStatus()
            ))
            .collect(Collectors.toList());
    }
}
