package com.shehia_management.api.letter;

import java.util.List;

/**
 * Owns the full letter lifecycle: application, review/approval, content
 * editing, PDF generation, and public verification.
 *
 * NOTE ON VERIFICATION: the original design sketch proposed a separate
 * LetterVerificationService. It was folded in here instead - verifying a
 * code only ever reads LetterApplication rows (via generateCode + a status
 * lookup), so a standalone "verification capability" would own no data of
 * its own and would just be a second consumer of LetterApplicationRepository.
 * That's a sign it isn't a separate capability, just a public-facing use
 * case of this one. VerificationController stays a distinct public
 * controller (that boundary IS meaningful - it's unauthenticated and has
 * no letter-application concerns) but it calls this service.
 */
public interface LetterService {

    LetterResponse applyForLetter(LetterApplicationRequest request, String authenticatedZanId);
    LetterResponse reviewLetter(String refNo, LetterStatus status, String adminComments);
    LetterResponse getLetterByRefNo(String refNo);
    String generateLetterHtml(String refNo);
    byte[] generateLetterPdf(String refNo);
    LetterResponse updateLetterContent(String refNo, String html);
    LetterResponse resetLetterContent(String refNo);
    String generateResidentLetterHtml(String refNo, String authenticatedZanId);
    byte[] generateResidentLetterPdf(String refNo, String authenticatedZanId);
    List<LetterResponse> getResidentLetters(String authenticatedZanId);
    List<LetterResponse> getAllLetters(LetterStatus status);

    /** Public, no-auth: looks up an approved/signed letter by its printed code. */
    LetterVerificationResponse verifyLetterByCode(String code);
}
