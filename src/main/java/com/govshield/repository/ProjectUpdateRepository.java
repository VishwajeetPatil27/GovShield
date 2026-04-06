package com.govshield.repository;

import com.govshield.model.ProjectUpdate;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;

@Repository
public interface ProjectUpdateRepository extends JpaRepository<ProjectUpdate, Long> {
    List<ProjectUpdate> findByProjectIdOrderByCreatedAtDesc(Long projectId);
}

