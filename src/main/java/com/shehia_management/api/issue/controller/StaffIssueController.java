package com.shehia_management.api.issue.controller;

import com.shehia_management.api.issue.*;
import com.shehia_management.api.staff.StaffService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * Staff-facing issue management, scoped to the caller's assigned zone.
 * Business rules live in IssueService; this controller resolves the
 * caller's zone via StaffService and hands off.
 */
@RestController
@RequestMapping("/api/v1/staff/issues")
@RequiredArgsConstructor
@PreAuthorize("hasRole('STAFF')")
public class StaffIssueController {

    private final IssueService issueService;
    private final StaffService staffService;

    @GetMapping
    public ResponseEntity<List<IssueResponse>> getIssues(
            Authentication authentication,
            @RequestParam(required = false) IssueStatus status) {
        String zone = staffService.requireAssignedZone(authentication.getName());
        return ResponseEntity.ok(issueService.getIssuesInZone(zone, status));
    }

    @GetMapping("/{repNo}")
    public ResponseEntity<IssueResponse> getIssue(Authentication authentication, @PathVariable String repNo) {
        String zone = staffService.requireAssignedZone(authentication.getName());
        return ResponseEntity.ok(issueService.getIssueInZone(repNo, zone));
    }

    @PutMapping("/{repNo}/status")
    public ResponseEntity<IssueResponse> updateIssueStatus(
            Authentication authentication,
            @PathVariable String repNo,
            @Valid @RequestBody IssueStatusRequest request) {
        String zone = staffService.requireAssignedZone(authentication.getName());
        return ResponseEntity.ok(issueService.updateIssueStatusInZone(repNo, request.getStatus(), zone));
    }

    @PutMapping("/{repNo}/assign")
    public ResponseEntity<IssueResponse> assignIssue(
            Authentication authentication,
            @PathVariable String repNo,
            @Valid @RequestBody IssueAssignmentRequest request) {
        String zone = staffService.requireAssignedZone(authentication.getName());
        return ResponseEntity.ok(issueService.assignIssueInZone(repNo, request.getOfficer(), zone));
    }

    @PutMapping("/{repNo}/priority")
    public ResponseEntity<IssueResponse> updateIssuePriority(
            Authentication authentication,
            @PathVariable String repNo,
            @Valid @RequestBody IssuePriorityRequest request) {
        String zone = staffService.requireAssignedZone(authentication.getName());
        return ResponseEntity.ok(issueService.updateIssuePriorityInZone(repNo, request.getPriority(), zone));
    }
}
