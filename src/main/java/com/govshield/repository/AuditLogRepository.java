package com.govshield.repository;

import com.govshield.model.AuditLog;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;

@Repository
public interface AuditLogRepository extends JpaRepository<AuditLog, Long> {
    List<AuditLog> findByEntityType(String entityType);
    List<AuditLog> findByAction(String action);
    List<AuditLog> findByPerformedBy(String performedBy);
}
