package com.shehia_management.api.resident.controller;

import com.shehia_management.api.identity.UserResponse;
import com.shehia_management.api.identity.UserStatus;
import com.shehia_management.api.identity.UserStatusRequest;
import com.shehia_management.api.resident.ResidentService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/admin/residents")
@RequiredArgsConstructor
@PreAuthorize("hasRole('ADMIN')")
public class AdminResidentController {

    private final ResidentService residentService;

    @GetMapping
    public ResponseEntity<List<UserResponse>> getAllResidents(@RequestParam(required = false) UserStatus status) {
        return ResponseEntity.ok(residentService.getAllResidents(status));
    }

    @GetMapping("/{id}")
    public ResponseEntity<UserResponse> getResident(@PathVariable Long id) {
        return ResponseEntity.ok(residentService.getResident(id));
    }

    @PutMapping("/{id}/status")
    public ResponseEntity<UserResponse> updateResidentStatus(
            @PathVariable Long id,
            @Valid @RequestBody UserStatusRequest request) {
        return ResponseEntity.ok(residentService.updateResidentStatus(id, request.getStatus()));
    }

    @PutMapping("/{id}/verify")
    public ResponseEntity<UserResponse> verifyResident(
            @PathVariable Long id,
            @RequestParam UserStatus status) {
        return ResponseEntity.ok(residentService.updateResidentStatus(id, status));
    }
}
