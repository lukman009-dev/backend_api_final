package com.shehia_management.api.issue;

import jakarta.persistence.*;
import com.shehia_management.api.identity.User;
import lombok.*;

import java.time.LocalDateTime;

@Entity
@Table(name = "issue_reports")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class IssueReport {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(unique = true, nullable = false)
    private String reportNumber; // e.g. REP-042

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "resident_id", nullable = false)
    private User resident;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private IssueCategory category;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private IssuePriority priority;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private IssueStatus status;

    private String location;

    @Column(columnDefinition = "TEXT", nullable = false)
    private String description;

    private String photoUrl;
    private String assignedOfficer;

    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    @PrePersist
    protected void onCreate() {
        this.createdAt = LocalDateTime.now();
        this.updatedAt = LocalDateTime.now();
    }

    @PreUpdate
    protected void onUpdate() {
        this.updatedAt = LocalDateTime.now();
    }
}
