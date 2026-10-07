package com.shehia_management.api.issue;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class IssueReportRequest {

    private Long residentId;
    private IssueCategory category;
    private IssuePriority priority;
    private String location;
    private String description;
    private String photoUrl; // optional
}
