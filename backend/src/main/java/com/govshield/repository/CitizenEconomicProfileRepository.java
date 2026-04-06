package com.govshield.repository;

import com.govshield.model.CitizenEconomicProfile;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.Optional;

@Repository
public interface CitizenEconomicProfileRepository extends JpaRepository<CitizenEconomicProfile, Long> {
    Optional<CitizenEconomicProfile> findByCitizenId(Long citizenId);
}

