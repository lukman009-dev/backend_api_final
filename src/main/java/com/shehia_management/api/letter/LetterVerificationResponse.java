package com.shehia_management.api.letter;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Returned by the public "verify this letter" endpoint. Deliberately
 * exposes only what someone checking a physical/downloaded letter needs
 * to confirm it's genuine — no phone number, national ID, or address.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class LetterVerificationResponse {

    private boolean valid;
    private String message;

    // Only populated when valid = true
    private String referenceNumber;
    private String letterType;
    private String residentName;
    private String status;
    private String issuedDate;
}
