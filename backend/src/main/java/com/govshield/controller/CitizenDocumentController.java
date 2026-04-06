package com.govshield.controller;

import com.govshield.dto.CitizenDocumentResponse;
import com.govshield.dto.CitizenDocumentUploadRequest;
import com.govshield.dto.CitizenDocumentVerifyRequest;
import com.govshield.service.CitizenDocumentService;
import com.govshield.util.RoleGuard;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/citizen-documents")
@CrossOrigin(origins = "*", maxAge = 3600)
public class CitizenDocumentController {

    @Autowired
    private CitizenDocumentService citizenDocumentService;

    @PostMapping("/upload")
    public ResponseEntity<CitizenDocumentResponse> uploadDocument(@RequestBody CitizenDocumentUploadRequest request,
                                                                  @RequestHeader(value = "X-User-Role", required = false) String role) {
        if (role != null && !role.isBlank()) {
            RoleGuard.ensureRole(role, "CITIZEN", "ADMIN", "OFFICER");
        }
        CitizenDocumentResponse response = citizenDocumentService.uploadDocument(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @GetMapping("/citizen/{citizenId}")
    public ResponseEntity<List<CitizenDocumentResponse>> getCitizenDocuments(@PathVariable Long citizenId,
                                                                             @RequestParam(defaultValue = "false") boolean includeContent,
                                                                             @RequestHeader("X-User-Role") String role) {
        RoleGuard.ensureRole(role, "CITIZEN", "ADMIN", "OFFICER", "AUDITOR");
        return ResponseEntity.ok(citizenDocumentService.getCitizenDocuments(citizenId, includeContent));
    }

    @GetMapping("/pending")
    public ResponseEntity<List<CitizenDocumentResponse>> getPendingDocuments(@RequestHeader("X-User-Role") String role) {
        RoleGuard.ensureRole(role, "ADMIN", "OFFICER");
        return ResponseEntity.ok(citizenDocumentService.getPendingDocuments());
    }

    @PostMapping("/{documentId}/verify")
    public ResponseEntity<CitizenDocumentResponse> verifyDocument(@PathVariable Long documentId,
                                                                  @RequestBody CitizenDocumentVerifyRequest request,
                                                                  @RequestHeader("X-User-Role") String role) {
        RoleGuard.ensureRole(role, "ADMIN", "OFFICER");
        return ResponseEntity.ok(citizenDocumentService.verifyDocument(documentId, request, role));
    }
}
