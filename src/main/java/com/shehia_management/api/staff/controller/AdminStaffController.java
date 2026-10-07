package com.shehia_management.api.staff.controller;

import com.shehia_management.api.identity.UserResponse;
import com.shehia_management.api.identity.UserStatus;
import com.shehia_management.api.identity.UserStatusRequest;
import com.shehia_management.api.staff.StaffService;
import com.shehia_management.api.staff.ZoneAssignmentRequest;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/admin/staff")
@RequiredArgsConstructor
@PreAuthorize("hasRole('ADMIN')")
public class AdminStaffController {

    private final StaffService staffService;

    @GetMapping
    public ResponseEntity<List<UserResponse>> getAllStaff(@RequestParam(required = false) UserStatus status) {
        return ResponseEntity.ok(staffService.getAllStaff(status));
    }

    @GetMapping("/{id}")
    public ResponseEntity<UserResponse> getStaff(@PathVariable Long id) {
        return ResponseEntity.ok(staffService.getStaffById(id));
    }

    @PutMapping("/{id}/status")
    public ResponseEntity<UserResponse> updateStaffStatus(
            @PathVariable Long id,
            @Valid @RequestBody UserStatusRequest request) {
        return ResponseEntity.ok(staffService.updateStaffStatus(id, request.getStatus()));
    }

    @PutMapping("/{id}/zone")
    public ResponseEntity<UserResponse> assignStaffZone(
            @PathVariable Long id,
            @Valid @RequestBody ZoneAssignmentRequest request) {
        return ResponseEntity.ok(staffService.assignStaffZone(id, request.getZone()));
    }
}
