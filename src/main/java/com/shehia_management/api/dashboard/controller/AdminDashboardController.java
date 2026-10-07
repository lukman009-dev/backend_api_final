package com.shehia_management.api.dashboard.controller;

import com.shehia_management.api.dashboard.ActivityPoint;
import com.shehia_management.api.dashboard.DashboardResponse;
import com.shehia_management.api.dashboard.DashboardService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/admin/dashboard")
@RequiredArgsConstructor
@PreAuthorize("hasRole('ADMIN')")
public class AdminDashboardController {

    private final DashboardService dashboardService;

    @GetMapping
    public ResponseEntity<DashboardResponse> getDashboard() {
        return ResponseEntity.ok(dashboardService.getDashboard());
    }

    @GetMapping("/activity")
    public ResponseEntity<List<ActivityPoint>> getActivity(@RequestParam(defaultValue = "14") int days) {
        return ResponseEntity.ok(dashboardService.getActivity(days));
    }
}
