package com.shehia_management.api.announcement.controller;

import com.shehia_management.api.announcement.*;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/admin/announcements")
@RequiredArgsConstructor
@PreAuthorize("hasRole('ADMIN')")
public class AdminAnnouncementController {

    private final AnnouncementService announcementService;

    @GetMapping
    public ResponseEntity<List<AnnouncementResponse>> getAnnouncements(
            @RequestParam(required = false) AnnouncementStatus status) {
        return ResponseEntity.ok(announcementService.getAllAnnouncements(status));
    }

    @GetMapping("/{id}")
    public ResponseEntity<AnnouncementResponse> getAnnouncement(@PathVariable Long id) {
        return ResponseEntity.ok(announcementService.getAnnouncement(id));
    }

    @PostMapping
    public ResponseEntity<AnnouncementResponse> createAnnouncement(@Valid @RequestBody AnnouncementRequest request) {
        return ResponseEntity.ok(announcementService.createAnnouncement(request));
    }

    @PutMapping("/{id}")
    public ResponseEntity<AnnouncementResponse> updateAnnouncement(
            @PathVariable Long id,
            @Valid @RequestBody AnnouncementRequest request) {
        return ResponseEntity.ok(announcementService.updateAnnouncement(id, request));
    }

    @PutMapping("/{id}/publish")
    public ResponseEntity<AnnouncementResponse> publishAnnouncement(@PathVariable Long id) {
        return ResponseEntity.ok(announcementService.publishAnnouncement(id));
    }

    @PutMapping("/{id}/unpublish")
    public ResponseEntity<AnnouncementResponse> unpublishAnnouncement(@PathVariable Long id) {
        return ResponseEntity.ok(announcementService.unpublishAnnouncement(id));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteAnnouncement(@PathVariable Long id) {
        announcementService.deleteAnnouncement(id);
        return ResponseEntity.noContent().build();
    }
}
