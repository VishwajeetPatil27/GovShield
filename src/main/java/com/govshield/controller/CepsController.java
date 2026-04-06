package com.govshield.controller;

import com.govshield.dto.CepsCalculateRequest;
import com.govshield.dto.CepsResponse;
import com.govshield.service.CepsService;
import com.govshield.util.RoleGuard;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/ceps")
@CrossOrigin(origins = "*", maxAge = 3600)
public class CepsController {

    @Autowired
    private CepsService cepsService;

    @GetMapping("/{ugid}")
    public ResponseEntity<CepsResponse> getCeps(@PathVariable String ugid,
                                                @RequestHeader(value = "X-User-Role", required = false) String role) {
        if (role != null && !role.isBlank()) {
            RoleGuard.ensureRole(role, "CITIZEN", "OFFICER", "AUDITOR", "ADMIN");
        }
        return ResponseEntity.ok(cepsService.getByUgid(ugid));
    }

    @PostMapping("/{ugid}/calculate")
    public ResponseEntity<CepsResponse> calculate(@PathVariable String ugid,
                                                  @RequestBody CepsCalculateRequest request,
                                                  @RequestHeader(value = "X-User-Role", required = false) String role) {
        if (role != null && !role.isBlank()) {
            RoleGuard.ensureRole(role, "CITIZEN", "OFFICER", "AUDITOR", "ADMIN");
        }
        return ResponseEntity.ok(cepsService.calculateAndUpsert(ugid, request));
    }
}

