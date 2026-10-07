package com.shehia_management.api.issue;

import com.shehia_management.api.identity.User;
import com.shehia_management.api.identity.UserLookupService;
import com.shehia_management.api.identity.ZoneUtil;
import com.shehia_management.api.shared.exception.ResourceNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;
import java.util.concurrent.ThreadLocalRandom;

@Service
@RequiredArgsConstructor
@Transactional
public class IssueServiceImpl implements IssueService {

    private final IssueReportRepository issueRepository;
    // Shared identity lookups: resolving "who is the authenticated resident"
    // and validating their account is not an issue-specific concern - see
    // UserLookupService for why this lives in the identity module.
    private final UserLookupService userLookupService;

    @Override
    public IssueResponse reportIssue(IssueReportRequest request, String authenticatedZanId) {
        User resident = userLookupService.requireActiveResident(authenticatedZanId);

        if (request.getCategory() == null || request.getPriority() == null
                || request.getDescription() == null || request.getDescription().isBlank()) {
            throw new IllegalArgumentException("Category, priority and description are required");
        }

        IssueReport issue = IssueReport.builder()
                .reportNumber(generateUniqueReportNumber())
                .resident(resident)
                .category(request.getCategory())
                .priority(request.getPriority())
                .location(request.getLocation())
                .description(request.getDescription())
                .photoUrl(request.getPhotoUrl())
                .status(IssueStatus.PENDING)
                .build();

        return toIssueResponse(issueRepository.save(issue));
    }

    @Override
    @Transactional(readOnly = true)
    public IssueResponse getIssueByReportNumber(String repNo) {
        return toIssueResponse(findIssue(repNo));
    }

    @Override
    public IssueResponse updateIssueStatus(String repNo, IssueStatus status) {
        IssueReport issue = findIssue(repNo);
        if (status == null) throw new IllegalArgumentException("Issue status is required");
        issue.setStatus(status);
        return toIssueResponse(issueRepository.save(issue));
    }

    @Override
    public IssueResponse assignIssue(String repNo, String officer) {
        IssueReport issue = findIssue(repNo);
        if (officer == null || officer.isBlank()) throw new IllegalArgumentException("Officer is required");
        issue.setAssignedOfficer(officer.trim());
        if (issue.getStatus() == IssueStatus.PENDING) issue.setStatus(IssueStatus.ASSIGNED);
        return toIssueResponse(issueRepository.save(issue));
    }

    @Override
    public IssueResponse updateIssuePriority(String repNo, IssuePriority priority) {
        IssueReport issue = findIssue(repNo);
        if (priority == null) throw new IllegalArgumentException("Issue priority is required");
        issue.setPriority(priority);
        return toIssueResponse(issueRepository.save(issue));
    }

    @Override
    @Transactional(readOnly = true)
    public List<IssueResponse> getResidentIssues(String authenticatedZanId) {
        User resident = userLookupService.findByZanId(authenticatedZanId);
        return issueRepository.findByResidentId(resident.getId()).stream().map(this::toIssueResponse).toList();
    }

    @Override
    @Transactional(readOnly = true)
    public List<IssueResponse> getAllIssues(IssueStatus status) {
        List<IssueReport> issues = status == null ? issueRepository.findAll() : issueRepository.findByStatus(status);
        return issues.stream().map(this::toIssueResponse).toList();
    }

    // ============================================================
    // ZONE-SCOPED ISSUE MANAGEMENT (staff)
    // ============================================================

    @Override
    @Transactional(readOnly = true)
    public List<IssueResponse> getIssuesInZone(String zone, IssueStatus status) {
        List<IssueReport> issues = status == null ? issueRepository.findAll() : issueRepository.findByStatus(status);
        return issues.stream()
                .filter(i -> zone.equalsIgnoreCase(ZoneUtil.extractZone(i.getResident().getHouseNumber())))
                .map(this::toIssueResponse)
                .toList();
    }

    @Override
    @Transactional(readOnly = true)
    public IssueResponse getIssueInZone(String repNo, String zone) {
        IssueReport issue = findIssue(repNo);
        ensureIssueInZone(issue, zone);
        return toIssueResponse(issue);
    }

    @Override
    public IssueResponse updateIssueStatusInZone(String repNo, IssueStatus status, String zone) {
        IssueReport issue = findIssue(repNo);
        ensureIssueInZone(issue, zone);
        if (status == null) throw new IllegalArgumentException("Issue status is required");
        issue.setStatus(status);
        return toIssueResponse(issueRepository.save(issue));
    }

    @Override
    public IssueResponse assignIssueInZone(String repNo, String officer, String zone) {
        IssueReport issue = findIssue(repNo);
        ensureIssueInZone(issue, zone);
        if (officer == null || officer.isBlank()) throw new IllegalArgumentException("Officer is required");
        issue.setAssignedOfficer(officer.trim());
        if (issue.getStatus() == IssueStatus.PENDING) issue.setStatus(IssueStatus.ASSIGNED);
        return toIssueResponse(issueRepository.save(issue));
    }

    @Override
    public IssueResponse updateIssuePriorityInZone(String repNo, IssuePriority priority, String zone) {
        IssueReport issue = findIssue(repNo);
        ensureIssueInZone(issue, zone);
        if (priority == null) throw new IllegalArgumentException("Issue priority is required");
        issue.setPriority(priority);
        return toIssueResponse(issueRepository.save(issue));
    }

    private void ensureIssueInZone(IssueReport issue, String zone) {
        String residentZone = ZoneUtil.extractZone(issue.getResident().getHouseNumber());
        if (residentZone == null || !residentZone.equalsIgnoreCase(zone)) {
            throw new AccessDeniedException("You are not allowed to manage issues outside your assigned zone");
        }
    }

    private IssueReport findIssue(String repNo) {
        return issueRepository.findByReportNumber(repNo)
                .orElseThrow(() -> new ResourceNotFoundException("Issue not found: " + repNo));
    }

    private String generateUniqueReportNumber() {
        String report;
        do {
            report = "REP-" + LocalDate.now().getYear() + "-" + String.format("%06d", ThreadLocalRandom.current().nextInt(1_000_000));
        } while (issueRepository.findByReportNumber(report).isPresent());
        return report;
    }

    private IssueResponse toIssueResponse(IssueReport issue) {
        User u = issue.getResident();
        return IssueResponse.builder()
                .id(issue.getId()).reportNumber(issue.getReportNumber())
                .resident(IssueResponse.UserSummary.builder().id(u.getId()).zanId(u.getZanId()).fullName(u.getFullName())
                        .phoneNumber(u.getPhoneNumber()).shehia(u.getShehia()).build())
                .category(issue.getCategory()).priority(issue.getPriority()).status(issue.getStatus())
                .location(issue.getLocation()).description(issue.getDescription()).photoUrl(issue.getPhotoUrl())
                .assignedOfficer(issue.getAssignedOfficer()).createdAt(issue.getCreatedAt()).updatedAt(issue.getUpdatedAt()).build();
    }
}
