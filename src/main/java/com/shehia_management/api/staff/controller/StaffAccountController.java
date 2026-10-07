package com.shehia_management.api.staff.controller;

import com.shehia_management.api.dashboard.DashboardResponse;
import com.shehia_management.api.dashboard.DashboardService;
import com.shehia_management.api.identity.UserResponse;
import com.shehia_management.api.staff.StaffRegisterRequest;
import com.shehia_management.api.staff.StaffService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

/**
 * Staff self-service: first-login registration, profile, and the
 * zone-scoped dashboard summary. A staff account is created here as
 * PENDING with no zone; once an admin assigns a zone and approves it
 * (see AdminStaffController), the staff member can sign in via the shared
 * /api/v1/auth/login endpoint.
 *
 * The dashboard endpoint delegates to DashboardService - reporting/summary
 * data is that capability's job regardless of which audience is asking.
 */
@RestController
@RequestMapping("/api/v1/staff")
@RequiredArgsConstructor
public class StaffAccountController {

    private final StaffService staffService;
    private final DashboardService dashboardService;

    // Public: first-login registration. Username + password only.
    @PostMapping("/register")
    public ResponseEntity<UserResponse> register(@Valid @RequestBody StaffRegisterRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(staffService.registerStaff(request));
    }

    @GetMapping("/profile")
    @PreAuthorize("hasRole('STAFF')")
    public ResponseEntity<UserResponse> getProfile(Authentication authentication) {
        return ResponseEntity.ok(staffService.getCurrentStaff(authentication.getName()));
    }

    @GetMapping("/dashboard")
    @PreAuthorize("hasRole('STAFF')")
    public ResponseEntity<DashboardResponse> dashboard(Authentication authentication) {
        String zone = staffService.requireAssignedZone(authentication.getName());
        return ResponseEntity.ok(dashboardService.getStaffDashboard(zone));
    }
}
