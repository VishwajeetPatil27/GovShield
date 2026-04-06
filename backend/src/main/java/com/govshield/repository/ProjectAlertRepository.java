package com.govshield.repository;

import com.govshield.model.ProjectAlert;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;

@Repository
public interface ProjectAlertRepository extends JpaRepository<ProjectAlert, Long> {
    List<ProjectAlert> findByResolvedFalseOrderByCreatedAtDesc();
    List<ProjectAlert> findByProjectIdOrderByCreatedAtDesc(Long projectId);
}

