package com.shehia_management.api.issue.controller;

import com.shehia_management.api.issue.IssueReportRequest;
import com.shehia_management.api.issue.IssueResponse;
import com.shehia_management.api.issue.IssueService;
import com.shehia_management.api.shared.storage.FileStorageService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

@RestController
@RequestMapping("/api/v1/resident/issues")
@RequiredArgsConstructor
public class IssueController {

    private final IssueService issueService;
    private final FileStorageService fileStorageService;

    // Accepts multipart/form-data with an optional "photo" file part, in
    // place of the resident typing a photoUrl string.
    @PostMapping(value = "/report", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<IssueResponse> reportIssue(
            @Valid @ModelAttribute IssueReportRequest request,
            @RequestParam(value = "photo", required = false) MultipartFile photo,
            Authentication authentication) {

        if (photo != null && !photo.isEmpty()) {
            request.setPhotoUrl(fileStorageService.store(photo, "issue-photos"));
        }

        return ResponseEntity.status(HttpStatus.CREATED)
                .body(issueService.reportIssue(request, authentication.getName()));
    }

    @GetMapping
    public ResponseEntity<List<IssueResponse>> getMyIssues(Authentication authentication) {
        return ResponseEntity.ok(issueService.getResidentIssues(authentication.getName()));
    }
}
