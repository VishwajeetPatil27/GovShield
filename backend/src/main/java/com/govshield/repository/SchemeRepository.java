package com.govshield.repository;

import com.govshield.model.Scheme;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.Optional;
import java.util.List;

@Repository
public interface SchemeRepository extends JpaRepository<Scheme, Long> {
    Optional<Scheme> findBySchemeCode(String schemeCode);
    List<Scheme> findBySector(String sector);
    List<Scheme> findByIsActive(Boolean isActive);
}
