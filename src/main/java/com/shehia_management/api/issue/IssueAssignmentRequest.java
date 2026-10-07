package com.shehia_management.api.issue;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class IssueAssignmentRequest {
    @NotBlank
    private String officer;
}
