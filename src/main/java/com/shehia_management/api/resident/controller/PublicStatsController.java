package com.shehia_management.api.resident.controller;

import com.shehia_management.api.resident.PublicStatsResponse;
import com.shehia_management.api.resident.ResidentService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * Public (no login) statistics for the landing page's hero card.
 * /api/v1/public/** is already permitted in SecurityConfig.
 */
@RestController
@RequestMapping("/api/v1/public/stats")
@RequiredArgsConstructor
public class PublicStatsController {

    private final ResidentService residentService;

    @GetMapping
    public ResponseEntity<PublicStatsResponse> getPublicStats() {
        return ResponseEntity.ok(residentService.getPublicStats());
    }
}
