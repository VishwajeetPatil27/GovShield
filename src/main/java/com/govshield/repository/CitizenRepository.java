package com.govshield.repository;

import com.govshield.model.Citizen;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.Optional;

@Repository
public interface CitizenRepository extends JpaRepository<Citizen, Long> {
    Optional<Citizen> findByUgid(String ugid);
    Optional<Citizen> findByAadhaar(String aadhaar);
    Optional<Citizen> findByEmail(String email);
    Optional<Citizen> findByPhoneNumber(String phoneNumber);
    Optional<Citizen> findByPan(String pan);
}
