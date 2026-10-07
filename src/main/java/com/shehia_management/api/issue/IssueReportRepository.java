package com.shehia_management.api.issue;

import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

public interface IssueReportRepository extends JpaRepository<IssueReport, Long> {
    Optional<IssueReport> findByReportNumber(String reportNumber);
    List<IssueReport> findByResidentId(Long residentId);
    List<IssueReport> findByStatus(IssueStatus status);
    long countByStatus(IssueStatus status);
    long countByPriority(IssuePriority priority);
    long countByCreatedAtGreaterThanEqualAndCreatedAtLessThan(LocalDateTime from, LocalDateTime to);
}
