package com.shehia_management.api.announcement;

import com.shehia_management.api.issue.IssuePriority;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;

import java.time.LocalDate;

@Data
public class AnnouncementRequest {
    @NotBlank
    private String title;
    @NotBlank
    private String content;
    private AnnouncementType type;
    // CHANGED: PriorityLevel -> IssuePriority (enum consolidation)
    private IssuePriority priority;
    private AnnouncementStatus status;
    private String targetShehia;
    private String imageUrl;
    private LocalDate expiryDate;
}
