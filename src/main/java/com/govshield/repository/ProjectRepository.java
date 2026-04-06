package com.govshield.repository;

import com.govshield.model.Project;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.Optional;
import java.util.List;

@Repository
public interface ProjectRepository extends JpaRepository<Project, Long> {
    Optional<Project> findByProjectCode(String projectCode);
    List<Project> findByAllocatedMla(String allocatedMla);
    List<Project> findByAllocatedMp(String allocatedMp);
    List<Project> findByStatus(String status);
    List<Project> findByState(String state);
}
