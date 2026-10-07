package com.shehia_management.api.letter;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;

/**
 * Generates the short, human-typeable verification code printed on every
 * approved letter (e.g. "8F9A-12B3-99C0").
 *
 * This is intentionally deterministic and reference-number-based:
 * - Same reference number -> always the same code, so it never needs to
 *   be stored anywhere; it can be recomputed on demand.
 * - It is NOT meant to be cryptographically unforgeable on its own — the
 *   actual security comes from requiring a matching, APPROVED/SIGNED
 *   record to exist in the database (see verification lookup). The code's
 *   job is just to be a short, unique, hard-to-guess pointer to that
 *   record for someone who can't/won't scan a QR code.
 *
 * Used by both PdfGeneratorService (to print the code on the letter) and
 * the public verification endpoint (to check a code someone typed in).
 */
public final class LetterVerificationUtil {

    private LetterVerificationUtil() {
    }

    public static String generateCode(String referenceNumber) {
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            byte[] hash = digest.digest(
                    referenceNumber.getBytes(StandardCharsets.UTF_8)
            );
            StringBuilder hex = new StringBuilder();
            for (int i = 0; i < 6 && i < hash.length; i++) {
                hex.append(String.format("%02X", hash[i]));
            }
            String raw = hex.toString();
            return raw.substring(0, 4) + "-" + raw.substring(4, 8) + "-" + raw.substring(8, 12);
        } catch (Exception e) {
            return "N/A";
        }
    }
}
