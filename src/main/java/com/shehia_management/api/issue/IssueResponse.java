package com.shehia_management.api.issue;

import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@Builder
public class IssueResponse {
    private Long id;
    private String reportNumber;
    private UserSummary resident;
    private IssueCategory category;
    private IssuePriority priority;
    private IssueStatus status;
    private String location;
    private String description;
    private String photoUrl;
    private String assignedOfficer;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    @Data
    @Builder
    public static class UserSummary {
        private Long id;
        private String zanId;
        private String fullName;
        private String phoneNumber;
        private String shehia;
    }
}
