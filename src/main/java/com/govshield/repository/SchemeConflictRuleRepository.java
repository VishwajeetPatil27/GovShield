package com.govshield.repository;

import com.govshield.model.SchemeConflictRule;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;

@Repository
public interface SchemeConflictRuleRepository extends JpaRepository<SchemeConflictRule, Long> {
    List<SchemeConflictRule> findByIsActiveTrue();
}

