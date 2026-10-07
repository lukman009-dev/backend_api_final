package com.shehia_management.api.letter.controller;

import com.shehia_management.api.letter.LetterApplicationRequest;
import com.shehia_management.api.letter.LetterResponse;
import com.shehia_management.api.letter.LetterService;
import com.shehia_management.api.shared.storage.FileStorageService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

@RestController
@RequestMapping("/api/v1/resident/letters")
@RequiredArgsConstructor
public class LetterController {

    private final LetterService letterService;
    private final FileStorageService fileStorageService;

    // Accepts multipart/form-data with an optional "supportingDoc" file
    // part, in place of the resident typing a supportingDocUrl string.
    @PostMapping(value = "/apply", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<LetterResponse> applyForLetter(
            @Valid @ModelAttribute LetterApplicationRequest request,
            @RequestParam(value = "supportingDoc", required = false) MultipartFile supportingDoc,
            Authentication authentication) {

        if (supportingDoc != null && !supportingDoc.isEmpty()) {
            request.setSupportingDocUrl(fileStorageService.store(supportingDoc, "supporting-docs"));
        }

        return ResponseEntity.status(HttpStatus.CREATED)
                .body(letterService.applyForLetter(request, authentication.getName()));
    }

    @GetMapping
    public ResponseEntity<List<LetterResponse>> getMyLetters(Authentication authentication) {
        return ResponseEntity.ok(letterService.getResidentLetters(authentication.getName()));
    }

    @GetMapping(value = "/{referenceNumber}/view", produces = MediaType.TEXT_HTML_VALUE)
    public ResponseEntity<String> viewMyLetterHtml(
            @PathVariable String referenceNumber,
            Authentication authentication) {
        return ResponseEntity.ok(letterService.generateResidentLetterHtml(referenceNumber, authentication.getName()));
    }

    @GetMapping(value = "/{referenceNumber}/download", produces = MediaType.APPLICATION_PDF_VALUE)
    public ResponseEntity<byte[]> downloadMyLetterPdf(
            @PathVariable String referenceNumber,
            Authentication authentication) {
        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=" + referenceNumber + ".pdf")
                .contentType(MediaType.APPLICATION_PDF)
                .body(letterService.generateResidentLetterPdf(referenceNumber, authentication.getName()));
    }
}
