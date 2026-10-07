package com.shehia_management.api.identity;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Entity
@Table(name = "users")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class User {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(unique = true, nullable = false)
    private String zanId;

    @Column(nullable = false)
    private String fullName;

    @Column(unique = true)
    private String email;

    private String phoneNumber;
    private String houseNumber;
    private String street;
    private String shehia;
    private String district;
    private String region;
    private LocalDate dateOfBirth;

    // MALE or FEMALE, chosen by the resident at registration. Kept nullable in
    // the database only so accounts created before this field existed (and the
    // admin/staff accounts) keep working; new residents must supply it - see
    // RegisterRequest.
    @Enumerated(EnumType.STRING)
    private Gender gender;

    private String password;
    @Column(nullable = false)
    @Builder.Default
    private boolean mustChangePassword = false;

    @Enumerated(EnumType.STRING)
    private Role role;

    @Enumerated(EnumType.STRING)
    private UserStatus status;

    private String idDocumentUrl;
    private String proofOfResidenceUrl;

    // Only meaningful for ROLE_STAFF accounts: the zone letter (A, B, C...)
    // this staff member is permitted to manage. Assigned by an admin after
    // the staff member self-registers. Null/blank means "not yet assigned"
    // (such an account cannot be activated - see StaffServiceImpl.updateStaffStatus).
    private String assignedZone;

    private LocalDateTime createdAt;

    @PrePersist
    protected void onCreate() {
        this.createdAt = LocalDateTime.now();
    }
}
