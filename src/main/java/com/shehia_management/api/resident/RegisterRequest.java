package com.shehia_management.api.resident;

import com.shehia_management.api.identity.Gender;
import com.shehia_management.api.identity.ZoneUtil;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import lombok.Data;
import jakarta.validation.constraints.Size;
import org.springframework.format.annotation.DateTimeFormat;

import java.time.LocalDate;

/**
 * Public-facing self-registration payload for residents.
 *
 * Replaces binding the raw User entity directly in ResidentController.register().
 * Only fields a resident should be able to supply appear here — role, status,
 * mustChangePassword, id, createdAt, and any generated document URLs are
 * deliberately absent and are always set server-side in ResidentServiceImpl.
 *
 * CHANGED: /register now takes multipart/form-data instead of JSON, so a
 * resident can attach a real ID document / proof-of-residence file (or a
 * photo taken with their camera) instead of typing a URL. This DTO's shape
 * is unchanged — ResidentController binds it via @ModelAttribute from the
 * form fields, then fills in idDocumentUrl/proofOfResidenceUrl itself from
 * whatever files were uploaded alongside it, via FileStorageService.
 */
@Data
public class RegisterRequest {

    @NotBlank
    private String zanId;

    @NotBlank
    private String fullName;

    @Email
    private String email;

    private String phoneNumber;

    // House numbers follow a fixed Shehia code: SH/UW/<ZONE LETTER>/<3-digit number>,
    // e.g. SH/UW/A/123. The zone letter also determines which staff member's
    // zone this resident falls under.
    @NotBlank
    @Pattern(
            regexp = ZoneUtil.HOUSE_NUMBER_REGEX,
            flags = Pattern.Flag.CASE_INSENSITIVE,
            message = "House number must look like SH/UW/A/123 (zone letter A-Z, 3-digit number other than 000)"
    )
    private String houseNumber;

    private String street;
    private String shehia;
    private String district;
    private String region;

    // NEW: explicit ISO (yyyy-MM-dd) format so this still binds correctly now
    // that /register is a multipart form post rather than JSON.
    @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
    private LocalDate dateOfBirth;

    // Required: MALE or FEMALE (send exactly these values in the form field
    // named "gender").
    @NotNull(message = "Gender is required (MALE or FEMALE)")
    private Gender gender;

    @NotBlank
    @Size(min = 6, message = "Password must be at least 6 characters")
    private String password;

    // Populated server-side by ResidentController from the uploaded
    // "idDocument" / "proofOfResidence" file parts (see FileStorageService) —
    // no longer typed in by the resident.
    private String idDocumentUrl;
    private String proofOfResidenceUrl;
}
