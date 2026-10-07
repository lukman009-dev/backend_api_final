package com.shehia_management.api.letter;

import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class LetterReviewRequest {
    @NotNull
    private LetterStatus status;
    private String adminComments;
}
