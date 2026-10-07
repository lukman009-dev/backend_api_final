package com.shehia_management.api.issue;

import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class IssuePriorityRequest {
    @NotNull
    private IssuePriority priority;
}
