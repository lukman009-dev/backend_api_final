package com.shehia_management.api.resident.controller;

import com.shehia_management.api.identity.UserResponse;
import com.shehia_management.api.identity.UserStatus;
import com.shehia_management.api.identity.UserStatusRequest;
import com.shehia_management.api.resident.RegisterRequest;
import com.shehia_management.api.resident.ResidentService;
import com.shehia_management.api.shared.storage.FileStorageService;
import com.shehia_management.api.staff.StaffService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

/**
 * Staff-facing resident management, scoped to the caller's assigned zone.
 * All the actual business rules (what "in my zone" means, how status
 * updates work, etc.) live in ResidentService - this controller's only job
 * is to resolve the caller's zone (via StaffService.requireAssignedZone)
 * and hand off.
 */
@RestController
@RequestMapping("/api/v1/staff/residents")
@RequiredArgsConstructor
@PreAuthorize("hasRole('STAFF')")
public class StaffResidentController {

    private final ResidentService residentService;
    private final StaffService staffService;
    private final FileStorageService fileStorageService;

    @GetMapping
    public ResponseEntity<List<UserResponse>> getResidents(
            Authentication authentication,
            @RequestParam(required = false) UserStatus status) {
        String zone = staffService.requireAssignedZone(authentication.getName());
        return ResponseEntity.ok(residentService.getResidentsInZone(zone, status));
    }

    @GetMapping("/{id}")
    public ResponseEntity<UserResponse> getResident(Authentication authentication, @PathVariable Long id) {
        String zone = staffService.requireAssignedZone(authentication.getName());
        return ResponseEntity.ok(residentService.getResidentInZone(id, zone));
    }

    // Accepts multipart/form-data so staff can attach an "idDocument" and/or
    // "proofOfResidence" file part alongside the regular form fields,
    // exactly like the resident self-registration endpoint.
    @PostMapping(value = "/register", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<UserResponse> registerResident(
            Authentication authentication,
            @Valid @ModelAttribute RegisterRequest request,
            @RequestParam(value = "idDocument", required = false) MultipartFile idDocument,
            @RequestParam(value = "proofOfResidence", required = false) MultipartFile proofOfResidence) {

        if (idDocument != null && !idDocument.isEmpty()) {
            request.setIdDocumentUrl(fileStorageService.store(idDocument, "id-documents"));
        }
        if (proofOfResidence != null && !proofOfResidence.isEmpty()) {
            request.setProofOfResidenceUrl(fileStorageService.store(proofOfResidence, "proof-of-residence"));
        }

        String zone = staffService.requireAssignedZone(authentication.getName());
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(residentService.registerResidentInZone(request, zone));
    }

    @PutMapping("/{id}/status")
    public ResponseEntity<UserResponse> updateResidentStatus(
            Authentication authentication,
            @PathVariable Long id,
            @Valid @RequestBody UserStatusRequest request) {
        String zone = staffService.requireAssignedZone(authentication.getName());
        return ResponseEntity.ok(residentService.updateResidentStatusInZone(id, request.getStatus(), zone));
    }
}
