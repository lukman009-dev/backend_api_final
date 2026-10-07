package com.shehia_management.api.announcement;

import jakarta.persistence.*;
import lombok.*;
import com.shehia_management.api.issue.IssuePriority; // NOTE: deliberately shared with the issue capability - see IssuePriority for rationale

import java.time.LocalDate;
import java.time.LocalDateTime;

@Entity
@Table(name = "announcements")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Announcement {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String title;

    @Column(columnDefinition = "TEXT", nullable = false)
    private String content;

    @Enumerated(EnumType.STRING)
    private AnnouncementType type;

    // CHANGED: was PriorityLevel (duplicate of IssuePriority: LOW/MEDIUM/HIGH/URGENT).
    // Consolidated onto the single IssuePriority enum used across the app.
    @Enumerated(EnumType.STRING)
    private IssuePriority priority;

    @Enumerated(EnumType.STRING)
    private AnnouncementStatus status;

    private String targetShehia;
    private String imageUrl;
    private LocalDate expiryDate;

    private LocalDateTime publishedAt;
}
