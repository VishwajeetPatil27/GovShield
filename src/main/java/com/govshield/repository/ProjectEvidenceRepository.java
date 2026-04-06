package com.govshield.repository;

import com.govshield.model.ProjectEvidence;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;

@Repository
public interface ProjectEvidenceRepository extends JpaRepository<ProjectEvidence, Long> {
    List<ProjectEvidence> findByProjectIdOrderByCreatedAtDesc(Long projectId);
    List<ProjectEvidence> findByCitizenIdOrderByCreatedAtDesc(Long citizenId);
}

