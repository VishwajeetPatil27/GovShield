package com.govshield.repository;

import com.govshield.model.Enrollment;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.Optional;
import java.util.List;

@Repository
public interface EnrollmentRepository extends JpaRepository<Enrollment, Long> {
    Optional<Enrollment> findByEnrollmentNumber(String enrollmentNumber);
    List<Enrollment> findByCitizenId(Long citizenId);
    List<Enrollment> findBySchemeId(Long schemeId);
    List<Enrollment> findByStatus(String status);
}
