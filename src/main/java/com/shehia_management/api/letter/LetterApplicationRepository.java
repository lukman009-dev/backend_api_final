package com.shehia_management.api.letter;

import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

public interface LetterApplicationRepository extends JpaRepository<LetterApplication, Long> {
    Optional<LetterApplication> findByReferenceNumber(String referenceNumber);
    List<LetterApplication> findByResidentId(Long residentId);
    List<LetterApplication> findByStatus(LetterStatus status);
    List<LetterApplication> findByStatusIn(List<LetterStatus> statuses);
    long countByStatus(LetterStatus status);
    long countBySubmittedAtGreaterThanEqualAndSubmittedAtLessThan(LocalDateTime from, LocalDateTime to);
}
