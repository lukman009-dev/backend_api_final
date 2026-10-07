package com.shehia_management.api.letter.controller;

import com.shehia_management.api.letter.*;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/admin/letters")
@RequiredArgsConstructor
@PreAuthorize("hasRole('ADMIN')")
public class AdminLetterController {

    private final LetterService letterService;

    @GetMapping
    public ResponseEntity<List<LetterResponse>> getAllLetters(@RequestParam(required = false) LetterStatus status) {
        return ResponseEntity.ok(letterService.getAllLetters(status));
    }

    @GetMapping("/{referenceNumber}")
    public ResponseEntity<LetterResponse> getLetter(@PathVariable String referenceNumber) {
        return ResponseEntity.ok(letterService.getLetterByRefNo(referenceNumber));
    }

    @PutMapping("/{referenceNumber}/review")
    public ResponseEntity<LetterResponse> reviewLetter(
            @PathVariable String referenceNumber,
            @Valid @RequestBody LetterReviewRequest request) {
        return ResponseEntity.ok(letterService.reviewLetter(referenceNumber, request.getStatus(), request.getAdminComments()));
    }

    @GetMapping(value = "/{referenceNumber}/preview", produces = MediaType.TEXT_HTML_VALUE)
    public ResponseEntity<String> previewLetterHtml(@PathVariable String referenceNumber) {
        return ResponseEntity.ok(letterService.generateLetterHtml(referenceNumber));
    }

    @PutMapping("/{referenceNumber}/content")
    public ResponseEntity<LetterResponse> updateLetterContent(
            @PathVariable String referenceNumber,
            @Valid @RequestBody LetterContentRequest request) {
        return ResponseEntity.ok(letterService.updateLetterContent(referenceNumber, request.getHtml()));
    }

    @DeleteMapping("/{referenceNumber}/content")
    public ResponseEntity<LetterResponse> resetLetterContent(@PathVariable String referenceNumber) {
        return ResponseEntity.ok(letterService.resetLetterContent(referenceNumber));
    }

    @GetMapping(value = "/{referenceNumber}/pdf", produces = MediaType.APPLICATION_PDF_VALUE)
    public ResponseEntity<byte[]> downloadLetterPdf(@PathVariable String referenceNumber) {
        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "inline; filename=" + referenceNumber + ".pdf")
                .contentType(MediaType.APPLICATION_PDF)
                .body(letterService.generateLetterPdf(referenceNumber));
    }
}
