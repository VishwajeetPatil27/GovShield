package com.govshield.repository;

import com.govshield.model.GovEmployee;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.Optional;

@Repository
public interface GovEmployeeRepository extends JpaRepository<GovEmployee, Long> {
    Optional<GovEmployee> findByEmail(String email);
    Optional<GovEmployee> findByEmployeeId(String employeeId);
}
