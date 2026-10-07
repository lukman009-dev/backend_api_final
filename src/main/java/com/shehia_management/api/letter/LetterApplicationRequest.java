package com.shehia_management.api.letter;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class LetterApplicationRequest {

    private Long residentId;
    private LetterType type;
    private String formData;

    // CHANGED: was "docUrl" (a plain string the resident typed in themselves).
    // Renamed to match the entity/response field it maps to. It's still
    // optional and still just a URL — ResidentController now fills it in
    // itself (via FileStorageService) after the resident uploads/photographs
    // a real file, instead of the resident supplying a URL directly.
    private String supportingDocUrl;
}
