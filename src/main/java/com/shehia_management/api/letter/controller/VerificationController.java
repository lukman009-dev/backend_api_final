package com.shehia_management.api.letter.controller;

import com.shehia_management.api.letter.LetterService;
import com.shehia_management.api.letter.LetterVerificationResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * Public, no-auth letter verification. Anyone holding a printed or
 * downloaded letter can type the "HASH:" code from the footer here to
 * confirm it's genuine.
 *
 * e.g. GET /api/v1/public/letters/verify/8F9A-12B3-99C0
 *
 * This stays its own controller (a distinct, unauthenticated public
 * use case) even though the logic behind it lives in LetterService -
 * see the note on LetterService for why verification isn't a separate
 * top-level capability/service.
 */
@RestController
@RequestMapping("/api/v1/public/letters")
@RequiredArgsConstructor
public class VerificationController {

    private final LetterService letterService;

    @GetMapping("/verify/{code}")
    public ResponseEntity<LetterVerificationResponse> verifyLetter(@PathVariable String code) {
        return ResponseEntity.ok(letterService.verifyLetterByCode(code));
    }
}
