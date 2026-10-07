package com.shehia_management.api.announcement.controller;

import com.shehia_management.api.announcement.AnnouncementResponse;
import com.shehia_management.api.announcement.AnnouncementService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/v1/public/announcements")
@RequiredArgsConstructor
public class PublicAnnouncementController {

    private final AnnouncementService announcementService;

    @GetMapping
    public ResponseEntity<List<AnnouncementResponse>> getPublicAnnouncements() {
        return ResponseEntity.ok(announcementService.getPublishedAnnouncements());
    }
}
