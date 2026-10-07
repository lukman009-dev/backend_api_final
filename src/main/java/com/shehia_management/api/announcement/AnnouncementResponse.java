package com.shehia_management.api.announcement;

import com.shehia_management.api.issue.IssuePriority;
import lombok.Builder;
import lombok.Data;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Data
@Builder
public class AnnouncementResponse {
    private Long id;
    private String title;
    private String content;
    private AnnouncementType type;
    // CHANGED: PriorityLevel -> IssuePriority (enum consolidation)
    private IssuePriority priority;
    private AnnouncementStatus status;
    private String targetShehia;
    private String imageUrl;
    private LocalDate expiryDate;
    private LocalDateTime publishedAt;
}
