package com.govshield.service;

import com.govshield.dto.CitizenDocumentResponse;
import com.govshield.dto.CitizenDocumentUploadRequest;
import com.govshield.dto.CitizenDocumentVerifyRequest;
import com.govshield.exception.CustomException;
import com.govshield.model.Citizen;
import com.govshield.model.CitizenDocument;
import com.govshield.repository.CitizenDocumentRepository;
import com.govshield.repository.CitizenRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Locale;

@Service
public class CitizenDocumentService {

    @Autowired
    private CitizenRepository citizenRepository;

    @Autowired
    private CitizenDocumentRepository citizenDocumentRepository;

    public CitizenDocumentResponse uploadDocument(CitizenDocumentUploadRequest request) {
        Citizen citizen = citizenRepository.findById(request.getCitizenId())
            .orElseThrow(() -> new CustomException("Citizen not found", "CITIZEN_NOT_FOUND", 404));

        validateUploadRequest(request);

        CitizenDocument document = new CitizenDocument();
        document.setCitizen(citizen);
        document.setDocumentType(request.getDocumentType().trim().toUpperCase(Locale.ROOT));
        document.setDocumentNumber(emptyToNull(request.getDocumentNumber()));
        document.setFileName(request.getFileName().trim());
        document.setFileContentBase64(request.getFileContentBase64().trim());
        document.setVerificationStatus("PENDING");
        document.setUploadedAt(LocalDateTime.now());

        CitizenDocument saved = citizenDocumentRepository.save(document);
        citizen.setVerificationStatus("UNDER_REVIEW");
        citizen.setUpdatedAt(LocalDate.now());
        citizenRepository.save(citizen);
        return toResponse(saved, false);
    }

    public List<CitizenDocumentResponse> getCitizenDocuments(Long citizenId, boolean includeContent) {
        citizenRepository.findById(citizenId)
            .orElseThrow(() -> new CustomException("Citizen not found", "CITIZEN_NOT_FOUND", 404));

        return citizenDocumentRepository.findByCitizenIdOrderByUploadedAtDesc(citizenId)
            .stream()
            .map(doc -> toResponse(doc, includeContent))
            .toList();
    }

    public List<CitizenDocumentResponse> getPendingDocuments() {
        return citizenDocumentRepository.findByVerificationStatusOrderByUploadedAtDesc("PENDING")
            .stream()
            .map(doc -> toResponse(doc, false))
            .toList();
    }

    public CitizenDocumentResponse verifyDocument(Long documentId, CitizenDocumentVerifyRequest request, String reviewerRole) {
        CitizenDocument document = citizenDocumentRepository.findById(documentId)
            .orElseThrow(() -> new CustomException("Document not found", "DOCUMENT_NOT_FOUND", 404));

        document.setVerificationStatus(request.isApproved() ? "VERIFIED" : "REJECTED");
        document.setVerificationRemarks(emptyToNull(request.getRemarks()));
        document.setVerifiedByRole(reviewerRole == null ? null : reviewerRole.toUpperCase(Locale.ROOT));
        document.setVerifiedAt(LocalDateTime.now());
        CitizenDocument saved = citizenDocumentRepository.save(document);

        refreshCitizenVerificationStatus(saved.getCitizen().getId());
        return toResponse(saved, false);
    }

    private void refreshCitizenVerificationStatus(Long citizenId) {
        Citizen citizen = citizenRepository.findById(citizenId)
            .orElseThrow(() -> new CustomException("Citizen not found", "CITIZEN_NOT_FOUND", 404));

        List<CitizenDocument> docs = citizenDocumentRepository.findByCitizenIdOrderByUploadedAtDesc(citizenId);
        if (docs.isEmpty()) {
            citizen.setVerificationStatus("PENDING");
        } else {
            boolean hasRejected = docs.stream().anyMatch(d -> "REJECTED".equalsIgnoreCase(d.getVerificationStatus()));
            boolean hasPending = docs.stream().anyMatch(d -> "PENDING".equalsIgnoreCase(d.getVerificationStatus()));
            boolean allVerified = docs.stream().allMatch(d -> "VERIFIED".equalsIgnoreCase(d.getVerificationStatus()));

            if (hasRejected) {
                citizen.setVerificationStatus("REJECTED");
            } else if (allVerified) {
                citizen.setVerificationStatus("VERIFIED");
            } else if (hasPending) {
                citizen.setVerificationStatus("UNDER_REVIEW");
            } else {
                citizen.setVerificationStatus("PENDING");
            }
        }
        citizen.setUpdatedAt(LocalDate.now());
        citizenRepository.save(citizen);
    }

    private CitizenDocumentResponse toResponse(CitizenDocument document, boolean includeContent) {
        Citizen citizen = document.getCitizen();
        String name = ((citizen.getFirstName() == null ? "" : citizen.getFirstName()) + " " +
            (citizen.getLastName() == null ? "" : citizen.getLastName())).trim();
        return new CitizenDocumentResponse(
            document.getId(),
            citizen.getId(),
            citizen.getUgid(),
            name,
            document.getDocumentType(),
            document.getDocumentNumber(),
            document.getFileName(),
            includeContent ? document.getFileContentBase64() : null,
            document.getVerificationStatus(),
            document.getVerificationRemarks(),
            document.getVerifiedByRole(),
            document.getUploadedAt(),
            document.getVerifiedAt()
        );
    }

    private void validateUploadRequest(CitizenDocumentUploadRequest request) {
        if (request.getCitizenId() == null) {
            throw new CustomException("Citizen ID is required", "VALIDATION_ERROR", 400);
        }
        if (request.getDocumentType() == null || request.getDocumentType().isBlank()) {
            throw new CustomException("Document type is required", "VALIDATION_ERROR", 400);
        }
        if (request.getFileName() == null || request.getFileName().isBlank()) {
            throw new CustomException("File name is required", "VALIDATION_ERROR", 400);
        }
        if (request.getFileContentBase64() == null || request.getFileContentBase64().isBlank()) {
            throw new CustomException("Document content is required", "VALIDATION_ERROR", 400);
        }
    }

    private String emptyToNull(String value) {
        if (value == null || value.isBlank()) {
            return null;
        }
        return value.trim();
    }
}
