package com.govshield.controller;

import com.govshield.model.AuditLog;
import com.govshield.repository.AuditLogRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.util.List;

@RestController
@RequestMapping("/api/audit")
@CrossOrigin(origins = "*", maxAge = 3600)
public class AuditController {

    @Autowired
    private AuditLogRepository auditLogRepository;

    /**
     * Create audit log
     */
    @PostMapping
    public ResponseEntity<AuditLog> createAuditLog(@RequestBody AuditLog auditLog) {
        AuditLog savedLog = auditLogRepository.save(auditLog);
        return ResponseEntity.ok(savedLog);
    }

    /**
     * Get all audit logs
     */
    @GetMapping
    public ResponseEntity<List<AuditLog>> getAllAuditLogs() {
        List<AuditLog> logs = auditLogRepository.findAll();
        return ResponseEntity.ok(logs);
    }

    /**
     * Get audit logs by entity type
     */
    @GetMapping("/entity/{entityType}")
    public ResponseEntity<List<AuditLog>> getAuditLogsByEntityType(@PathVariable String entityType) {
        List<AuditLog> logs = auditLogRepository.findByEntityType(entityType);
        return ResponseEntity.ok(logs);
    }

    /**
     * Get audit logs by action
     */
    @GetMapping("/action/{action}")
    public ResponseEntity<List<AuditLog>> getAuditLogsByAction(@PathVariable String action) {
        List<AuditLog> logs = auditLogRepository.findByAction(action);
        return ResponseEntity.ok(logs);
    }

    /**
     * Get audit logs by performed by
     */
    @GetMapping("/performer/{performedBy}")
    public ResponseEntity<List<AuditLog>> getAuditLogsByPerformedBy(@PathVariable String performedBy) {
        List<AuditLog> logs = auditLogRepository.findByPerformedBy(performedBy);
        return ResponseEntity.ok(logs);
    }
}
