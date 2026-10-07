package com.shehia_management.api.issue.controller;

import com.shehia_management.api.issue.*;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/admin/issues")
@RequiredArgsConstructor
@PreAuthorize("hasRole('ADMIN')")
public class AdminIssueController {

    private final IssueService issueService;

    @GetMapping
    public ResponseEntity<List<IssueResponse>> getAllIssues(@RequestParam(required = false) IssueStatus status) {
        return ResponseEntity.ok(issueService.getAllIssues(status));
    }

    @GetMapping("/{repNo}")
    public ResponseEntity<IssueResponse> getIssue(@PathVariable String repNo) {
        return ResponseEntity.ok(issueService.getIssueByReportNumber(repNo));
    }

    @PutMapping("/{repNo}/status")
    public ResponseEntity<IssueResponse> updateIssueStatus(
            @PathVariable String repNo,
            @Valid @RequestBody IssueStatusRequest request) {
        return ResponseEntity.ok(issueService.updateIssueStatus(repNo, request.getStatus()));
    }

    @PutMapping("/{repNo}/assign")
    public ResponseEntity<IssueResponse> assignIssue(
            @PathVariable String repNo,
            @Valid @RequestBody IssueAssignmentRequest request) {
        return ResponseEntity.ok(issueService.assignIssue(repNo, request.getOfficer()));
    }

    @PutMapping("/{repNo}/priority")
    public ResponseEntity<IssueResponse> updateIssuePriority(
            @PathVariable String repNo,
            @Valid @RequestBody IssuePriorityRequest request) {
        return ResponseEntity.ok(issueService.updateIssuePriority(repNo, request.getPriority()));
    }
}
