package com.govshield.service;

import com.govshield.model.SchemeConflictRule;
import com.govshield.repository.SchemeConflictRuleRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;

@Service
public class SchemeConflictService {

    public static class ConflictResult {
        private final Long ruleId;
        private final String action;
        private final String message;

        public ConflictResult(Long ruleId, String action, String message) {
            this.ruleId = ruleId;
            this.action = action;
            this.message = message;
        }

        public Long getRuleId() {
            return ruleId;
        }

        public String getAction() {
            return action;
        }

        public String getMessage() {
            return message;
        }
    }

    @Autowired
    private SchemeConflictRuleRepository conflictRuleRepository;

    public Optional<ConflictResult> findConflict(String newSector, List<String> existingSectors) {
        String incoming = normalize(newSector);
        if (incoming.isBlank() || existingSectors == null || existingSectors.isEmpty()) return Optional.empty();

        List<SchemeConflictRule> rules = conflictRuleRepository.findByIsActiveTrue();
        return existingSectors.stream()
            .map(this::normalize)
            .filter(s -> !s.isBlank())
            .flatMap(existing -> rules.stream()
                .filter(rule -> matches(rule, incoming, existing))
                .map(rule -> new ConflictResult(rule.getId(), normalize(rule.getAction()), rule.getMessage())))
            .max(Comparator.comparingInt(r -> "REJECT".equalsIgnoreCase(r.getAction()) ? 2 : 1));
    }

    private boolean matches(SchemeConflictRule rule, String a, String b) {
        String ra = normalize(rule.getSectorA());
        String rb = normalize(rule.getSectorB());
        return (ra.equalsIgnoreCase(a) && rb.equalsIgnoreCase(b)) || (ra.equalsIgnoreCase(b) && rb.equalsIgnoreCase(a));
    }

    private String normalize(String value) {
        return value == null ? "" : value.trim().toUpperCase();
    }
}

