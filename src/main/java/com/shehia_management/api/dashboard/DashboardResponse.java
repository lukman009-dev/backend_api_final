package com.shehia_management.api.dashboard;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class DashboardResponse {
    private long totalResidents;
    private long pendingResidents;
    private long activeResidents;
    private long suspendedResidents;
    private long totalLetters;
    private long pendingLetters;
    private long totalIssues;
    private long pendingIssues;
    private long urgentIssues;
    private long totalAnnouncements;
    private long publishedAnnouncements;
}
