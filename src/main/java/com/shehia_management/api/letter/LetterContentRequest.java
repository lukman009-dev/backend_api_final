package com.shehia_management.api.letter;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class LetterContentRequest {

    @NotBlank(message = "Letter HTML content is required")
    private String html;
}
