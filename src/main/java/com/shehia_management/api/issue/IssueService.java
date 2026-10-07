package com.shehia_management.api.issue;

import java.util.List;

/**
 * Owns every business rule for the issue-report lifecycle: a resident
 * reporting an issue, an admin managing all issues, or a staff member
 * managing only the issues in their zone. Same rationale as ResidentService:
 * one authoritative place per capability regardless of caller.
 */
public interface IssueService {

    IssueResponse reportIssue(IssueReportRequest request, String authenticatedZanId);
    IssueResponse getIssueByReportNumber(String repNo);
    IssueResponse updateIssueStatus(String repNo, IssueStatus status);
    IssueResponse assignIssue(String repNo, String officer);
    IssueResponse updateIssuePriority(String repNo, IssuePriority priority);
    List<IssueResponse> getResidentIssues(String authenticatedZanId);
    List<IssueResponse> getAllIssues(IssueStatus status);

    // Zone-scoped variants used by staff dashboards
    List<IssueResponse> getIssuesInZone(String zone, IssueStatus status);
    IssueResponse getIssueInZone(String repNo, String zone);
    IssueResponse updateIssueStatusInZone(String repNo, IssueStatus status, String zone);
    IssueResponse assignIssueInZone(String repNo, String officer, String zone);
    IssueResponse updateIssuePriorityInZone(String repNo, IssuePriority priority, String zone);
}
