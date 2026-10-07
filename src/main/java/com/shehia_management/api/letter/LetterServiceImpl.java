package com.shehia_management.api.letter;

import com.shehia_management.api.identity.User;
import com.shehia_management.api.identity.UserLookupService;
import com.shehia_management.api.shared.exception.ResourceNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.io.File;
import java.io.FileOutputStream;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.concurrent.ThreadLocalRandom;

@Service
@RequiredArgsConstructor
@Transactional
public class LetterServiceImpl implements LetterService {

    private final LetterApplicationRepository letterRepository;
    private final PdfGeneratorService pdfGeneratorService;
    private final UserLookupService userLookupService;

    @Override
    public LetterResponse applyForLetter(LetterApplicationRequest request, String authenticatedZanId) {
        User resident = userLookupService.requireActiveResident(authenticatedZanId);

        if (request.getType() == null) {
            throw new IllegalArgumentException("Letter type is required");
        }

        LetterApplication application = LetterApplication.builder()
                .referenceNumber(generateUniqueReferenceNumber())
                .resident(resident)
                .letterType(request.getType())
                .dynamicFormData(request.getFormData())
                .supportingDocUrl(request.getSupportingDocUrl())
                .status(LetterStatus.PENDING)
                .build();

        return toLetterResponse(letterRepository.save(application));
    }

    @Override
    public LetterResponse reviewLetter(String refNo, LetterStatus status, String adminComments) {
        LetterApplication letter = findLetter(refNo);
        if (status == null) {
            throw new IllegalArgumentException("Letter status is required");
        }

        letter.setStatus(status);
        letter.setAdminComments(adminComments);
        letter.setReviewedAt(LocalDateTime.now());

        if (status == LetterStatus.APPROVED || status == LetterStatus.SIGNED) {
            try {
                byte[] pdfBytes = pdfGeneratorService.generateLetterPdf(letter);
                String filename = refNo + ".pdf";
                File dir = new File("uploads/letters/");
                if (!dir.exists() && !dir.mkdirs()) {
                    throw new IllegalStateException("Could not create letter upload directory");
                }
                try (FileOutputStream fos = new FileOutputStream(new File(dir, filename))) {
                    fos.write(pdfBytes);
                }
                letter.setGeneratedDocumentUrl("/docs/generated/" + filename);
            } catch (Exception e) {
                throw new IllegalStateException("Failed to generate letter PDF: " + e.getMessage(), e);
            }
        }

        return toLetterResponse(letterRepository.save(letter));
    }

    @Override
    @Transactional(readOnly = true)
    public LetterResponse getLetterByRefNo(String refNo) {
        return toLetterResponse(findLetter(refNo));
    }

    @Override
    @Transactional(readOnly = true)
    public String generateLetterHtml(String refNo) {
        return pdfGeneratorService.generateLetterHtml(findLetter(refNo));
    }

    @Override
    @Transactional(readOnly = true)
    public byte[] generateLetterPdf(String refNo) {
        return pdfGeneratorService.generateLetterPdf(findLetter(refNo));
    }

    @Override
    public LetterResponse updateLetterContent(String refNo, String html) {
        LetterApplication letter = findLetter(refNo);
        if (html == null || html.isBlank()) {
            throw new IllegalArgumentException("Letter HTML content is required");
        }
        letter.setEditedContentHtml(sanitizeAdminHtml(html));
        return toLetterResponse(letterRepository.save(letter));
    }

    @Override
    public LetterResponse resetLetterContent(String refNo) {
        LetterApplication letter = findLetter(refNo);
        letter.setEditedContentHtml(null);
        return toLetterResponse(letterRepository.save(letter));
    }

    @Override
    @Transactional(readOnly = true)
    public String generateResidentLetterHtml(String refNo, String authenticatedZanId) {
        LetterApplication letter = findLetter(refNo);
        ensureLetterBelongsToResident(letter, authenticatedZanId);
        ensureLetterApproved(letter);
        return pdfGeneratorService.generateLetterHtml(letter);
    }

    @Override
    @Transactional(readOnly = true)
    public byte[] generateResidentLetterPdf(String refNo, String authenticatedZanId) {
        LetterApplication letter = findLetter(refNo);
        ensureLetterBelongsToResident(letter, authenticatedZanId);
        ensureLetterApproved(letter);
        return pdfGeneratorService.generateLetterPdf(letter);
    }

    @Override
    @Transactional(readOnly = true)
    public List<LetterResponse> getResidentLetters(String authenticatedZanId) {
        User resident = userLookupService.findByZanId(authenticatedZanId);
        return letterRepository.findByResidentId(resident.getId()).stream().map(this::toLetterResponse).toList();
    }

    @Override
    @Transactional(readOnly = true)
    public List<LetterResponse> getAllLetters(LetterStatus status) {
        List<LetterApplication> letters = status == null ? letterRepository.findAll() : letterRepository.findByStatus(status);
        return letters.stream().map(this::toLetterResponse).toList();
    }

    // ============================================================
    // VERIFICATION (public, no-auth) - see class-level note on why this
    // lives here rather than in a separate capability/service.
    // ============================================================

    @Override
    @Transactional(readOnly = true)
    public LetterVerificationResponse verifyLetterByCode(String code) {

        if (code == null || code.isBlank()) {
            return LetterVerificationResponse.builder()
                    .valid(false)
                    .message("No verification code provided.")
                    .build();
        }

        String normalizedInput = code.trim().toUpperCase();

        // The code is deterministically derived from the reference number
        // (see LetterVerificationUtil), not stored anywhere, so we find a
        // match by recomputing it for every approved/signed letter. Fine
        // for a single-Shehia office's letter volume; if this ever needs
        // to scale, store the code as a column and query it directly
        // instead.
        List<LetterApplication> candidates = letterRepository.findByStatusIn(
                List.of(LetterStatus.APPROVED, LetterStatus.SIGNED)
        );

        for (LetterApplication letter : candidates) {
            String expected = LetterVerificationUtil.generateCode(letter.getReferenceNumber());

            if (expected.equalsIgnoreCase(normalizedInput)) {
                return LetterVerificationResponse.builder()
                        .valid(true)
                        .message("This letter is genuine and was issued by this Shehia office.")
                        .referenceNumber(letter.getReferenceNumber())
                        .letterType(letter.getLetterType().name())
                        .residentName(letter.getResident().getFullName())
                        .status(letter.getStatus().name())
                        .issuedDate(
                                letter.getReviewedAt() != null
                                        ? letter.getReviewedAt().toLocalDate().toString()
                                        : (letter.getSubmittedAt() != null
                                        ? letter.getSubmittedAt().toLocalDate().toString()
                                        : "")
                        )
                        .build();
            }
        }

        return LetterVerificationResponse.builder()
                .valid(false)
                .message("No matching letter found. This code may be invalid, or the letter may not have been issued by this office.")
                .build();
    }

    // Admin-only content, but it still gets rendered raw (both in the
    // browser preview and fed straight into the PDF renderer), so strip
    // the obvious script/event-handler injection vectors as a
    // defense-in-depth measure. This is intentionally lightweight - not
    // a substitute for a proper HTML sanitizer if this input surface is
    // ever exposed beyond trusted admin accounts.
    private String sanitizeAdminHtml(String html) {
        String cleaned = html.replaceAll("(?is)<script.*?>.*?</script>", "");
        cleaned = cleaned.replaceAll("(?is)\\son\\w+\\s*=\\s*\"[^\"]*\"", "");
        cleaned = cleaned.replaceAll("(?is)\\son\\w+\\s*=\\s*'[^']*'", "");
        return cleaned;
    }

    private LetterApplication findLetter(String refNo) {
        return letterRepository.findByReferenceNumber(refNo)
                .orElseThrow(() -> new ResourceNotFoundException("Letter not found: " + refNo));
    }

    private void ensureLetterBelongsToResident(LetterApplication letter, String zanId) {
        if (!letter.getResident().getZanId().equals(zanId)) {
            throw new AccessDeniedException("You are not allowed to access this letter");
        }
    }

    private void ensureLetterApproved(LetterApplication letter) {
        if (letter.getStatus() != LetterStatus.APPROVED && letter.getStatus() != LetterStatus.SIGNED) {
            throw new AccessDeniedException("This letter has not been approved yet");
        }
    }

    private String generateUniqueReferenceNumber() {
        String reference;
        do {
            reference = "REF-" + LocalDate.now().getYear() + "-" + String.format("%06d", ThreadLocalRandom.current().nextInt(1_000_000));
        } while (letterRepository.findByReferenceNumber(reference).isPresent());
        return reference;
    }

    private LetterResponse toLetterResponse(LetterApplication letter) {
        User u = letter.getResident();
        return LetterResponse.builder()
                .id(letter.getId()).referenceNumber(letter.getReferenceNumber())
                .resident(LetterResponse.UserSummary.builder().id(u.getId()).zanId(u.getZanId()).fullName(u.getFullName())
                        .email(u.getEmail()).phoneNumber(u.getPhoneNumber()).shehia(u.getShehia()).build())
                .letterType(letter.getLetterType()).status(letter.getStatus()).dynamicFormData(letter.getDynamicFormData())
                .supportingDocUrl(letter.getSupportingDocUrl()).generatedDocumentUrl(letter.getGeneratedDocumentUrl())
                .adminComments(letter.getAdminComments())
                .customContent(letter.getEditedContentHtml() != null && !letter.getEditedContentHtml().isBlank())
                .submittedAt(letter.getSubmittedAt()).reviewedAt(letter.getReviewedAt()).build();
    }
}
