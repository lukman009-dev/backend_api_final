package com.shehia_management.api.identity;

import lombok.Builder;
import lombok.Data;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Data
@Builder
public class UserResponse {
    private Long id;
    private String zanId;
    private String fullName;
    private String email;
    private String phoneNumber;
    private String houseNumber;
    private String street;
    private String shehia;
    private String district;
    private String region;
    private LocalDate dateOfBirth;
    private Gender gender;
    private Role role;
    private UserStatus status;
    private String idDocumentUrl;
    private String proofOfResidenceUrl;
    private LocalDateTime createdAt;
    private boolean mustChangePassword;
    // Only meaningful for ROLE_STAFF accounts.
    private String assignedZone;
}
