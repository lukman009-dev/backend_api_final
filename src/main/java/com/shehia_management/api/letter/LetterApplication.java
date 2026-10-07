package com.shehia_management.api.letter;

import jakarta.persistence.*;
import com.shehia_management.api.identity.User;
import lombok.*;

import java.time.LocalDateTime;

@Entity
@Table(name = "letter_applications")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class LetterApplication {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(unique = true, nullable = false)
    private String referenceNumber; // e.g. REF-2026-991

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "resident_id", nullable = false)
    private User resident;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private LetterType letterType;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private LetterStatus status;

    @Column(columnDefinition = "TEXT")
    private String dynamicFormData;

    private String supportingDocUrl;
    private String generatedDocumentUrl;

    @Column(columnDefinition = "TEXT")
    private String adminComments;

    // Full, self-contained HTML document saved by an admin after editing
    // the letter in the review screen's preview. When present, this is
    // used verbatim for both the HTML preview and the PDF, in place of
    // regenerating from the Thymeleaf template - until an admin resets
    // it. Null/blank means "use the template as normal".
    @Column(columnDefinition = "LONGTEXT")
    private String editedContentHtml;

    private LocalDateTime submittedAt;
    private LocalDateTime reviewedAt;

    @PrePersist
    protected void onCreate() {
        this.submittedAt = LocalDateTime.now();
    }
}
