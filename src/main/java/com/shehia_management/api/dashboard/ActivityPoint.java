package com.shehia_management.api.dashboard;

import lombok.AllArgsConstructor;
import lombok.Data;

import java.time.LocalDate;

@Data
@AllArgsConstructor
public class ActivityPoint {
    private LocalDate date;
    private long residents;
    private long letters;
    private long issues;
    private long announcements;
}
