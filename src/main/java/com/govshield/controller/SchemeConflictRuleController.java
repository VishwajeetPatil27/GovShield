package com.govshield.controller;

import com.govshield.model.SchemeConflictRule;
import com.govshield.repository.SchemeConflictRuleRepository;
import com.govshield.util.RoleGuard;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.time.LocalDateTime;
import java.util.List;

@RestController
@RequestMapping("/api/conflicts")
@CrossOrigin(origins = "*", maxAge = 3600)
public class SchemeConflictRuleController {

    @Autowired
    private SchemeConflictRuleRepository repository;

    @GetMapping("/rules")
    public ResponseEntity<List<SchemeConflictRule>> listActive(@RequestHeader("X-User-Role") String role) {
        RoleGuard.ensureRole(role, "OFFICER", "ADMIN", "AUDITOR");
        return ResponseEntity.ok(repository.findByIsActiveTrue());
    }

    @PostMapping("/rules")
    public ResponseEntity<SchemeConflictRule> create(@RequestBody SchemeConflictRule rule,
                                                     @RequestHeader("X-User-Role") String role) {
        RoleGuard.ensureRole(role, "OFFICER", "ADMIN");
        rule.setId(null);
        rule.setSectorA(rule.getSectorA() == null ? null : rule.getSectorA().trim().toUpperCase());
        rule.setSectorB(rule.getSectorB() == null ? null : rule.getSectorB().trim().toUpperCase());
        rule.setAction(rule.getAction() == null ? "REJECT" : rule.getAction().trim().toUpperCase());
        rule.setCreatedAt(LocalDateTime.now());
        rule.setUpdatedAt(LocalDateTime.now());
        return ResponseEntity.status(HttpStatus.CREATED).body(repository.save(rule));
    }
}

