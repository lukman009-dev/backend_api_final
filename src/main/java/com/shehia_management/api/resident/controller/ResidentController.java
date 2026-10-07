package com.shehia_management.api.resident.controller;

import com.shehia_management.api.identity.UserResponse;
import com.shehia_management.api.resident.RegisterRequest;
import com.shehia_management.api.resident.ResidentService;
import com.shehia_management.api.shared.storage.FileStorageService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

/**
 * Public, resident-facing self-service: registration and profile only.
 * Letter application and issue reporting used to live on this same
 * controller in the old code but belong to their own capabilities - see
 * com.shehia_management.api.letter.controller.LetterController and
 * com.shehia_management.api.issue.controller.IssueController.
 */
@RestController
@RequestMapping("/api/v1/resident")
@RequiredArgsConstructor
public class ResidentController {

    private final ResidentService residentService;
    // Stores uploaded photos/documents and hands back a URL — see
    // FileStorageService for the validation + storage rules.
    private final FileStorageService fileStorageService;

    // Accepts multipart/form-data: the regular form fields, plus optional
    // "idDocument" and "proofOfResidence" file parts (a picked file or a
    // camera capture). Each uploaded file is validated and stored by
    // FileStorageService, and the resulting URL is set on the request before
    // it's handed to the service.
    @PostMapping(value = "/register", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<UserResponse> register(
            @Valid @ModelAttribute RegisterRequest request,
            @RequestParam(value = "idDocument", required = false) MultipartFile idDocument,
            @RequestParam(value = "proofOfResidence", required = false) MultipartFile proofOfResidence) {

        if (idDocument != null && !idDocument.isEmpty()) {
            request.setIdDocumentUrl(fileStorageService.store(idDocument, "id-documents"));
        }
        if (proofOfResidence != null && !proofOfResidence.isEmpty()) {
            request.setProofOfResidenceUrl(fileStorageService.store(proofOfResidence, "proof-of-residence"));
        }

        return ResponseEntity.status(HttpStatus.CREATED).body(residentService.registerResident(request));
    }

    @GetMapping("/profile")
    public ResponseEntity<UserResponse> getProfile(Authentication authentication) {
        return ResponseEntity.ok(residentService.getCurrentResident(authentication.getName()));
    }
}
