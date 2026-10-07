package com.shehia_management.api.issue;

import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class IssueStatusRequest {
    @NotNull
    private IssueStatus status;
}
