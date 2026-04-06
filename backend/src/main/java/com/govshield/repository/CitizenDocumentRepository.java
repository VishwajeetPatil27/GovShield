package com.govshield.repository;

import com.govshield.model.CitizenDocument;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface CitizenDocumentRepository extends JpaRepository<CitizenDocument, Long> {
    List<CitizenDocument> findByCitizenIdOrderByUploadedAtDesc(Long citizenId);
    List<CitizenDocument> findByVerificationStatusOrderByUploadedAtDesc(String verificationStatus);
}
