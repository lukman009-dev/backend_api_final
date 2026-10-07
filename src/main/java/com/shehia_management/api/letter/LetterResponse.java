package com.shehia_management.api.letter;

import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@Builder
public class LetterResponse {
    private Long id;
    private String referenceNumber;
    private UserSummary resident;
    private LetterType letterType;
    private LetterStatus status;
    private String dynamicFormData;
    private String supportingDocUrl;
    private String generatedDocumentUrl;
    private String adminComments;
    // True once an admin has hand-edited this letter's content in the
    // review screen; the frontend uses this to show an "edited" badge
    // and enable the "discard edits" control.
    private boolean customContent;
    private LocalDateTime submittedAt;
    private LocalDateTime reviewedAt;

    @Data
    @Builder
    public static class UserSummary {
        private Long id;
        private String zanId;
        private String fullName;
        private String email;
        private String phoneNumber;
        private String shehia;
    }
}
