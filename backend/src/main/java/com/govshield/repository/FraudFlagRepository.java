package com.govshield.repository;

import com.govshield.model.FraudFlag;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.Optional;

@Repository
public interface FraudFlagRepository extends JpaRepository<FraudFlag, Long> {
    Optional<FraudFlag> findByCitizen_Id(Long citizenId);
}
