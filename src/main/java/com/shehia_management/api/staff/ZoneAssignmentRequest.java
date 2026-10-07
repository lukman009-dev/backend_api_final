package com.shehia_management.api.staff;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import lombok.Data;

@Data
public class ZoneAssignmentRequest {

    @NotBlank
    @Pattern(regexp = "^[A-Za-z]$", message = "Zone must be a single letter, e.g. A, B, C")
    private String zone;
}
