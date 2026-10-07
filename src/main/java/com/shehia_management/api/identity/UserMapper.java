package com.shehia_management.api.identity;

/**
 * EXTRACTED SHARED LOGIC: this was a private `toUserResponse(User)` method
 * duplicated in spirit across the old god-service's resident and staff
 * sections (both just build a UserResponse from a User). Since User is a
 * single shared entity used by both the resident and staff capabilities,
 * the mapping lives here in the identity module instead of being owned by
 * (or copy-pasted into) either capability.
 */
public final class UserMapper {

    private UserMapper() {
    }

    public static UserResponse toResponse(User user) {
        return UserResponse.builder()
                .id(user.getId()).zanId(user.getZanId()).fullName(user.getFullName())
                .email(user.getEmail()).phoneNumber(user.getPhoneNumber()).houseNumber(user.getHouseNumber())
                .street(user.getStreet()).shehia(user.getShehia()).district(user.getDistrict()).region(user.getRegion())
                .dateOfBirth(user.getDateOfBirth()).gender(user.getGender()).role(user.getRole()).status(user.getStatus())
                .idDocumentUrl(user.getIdDocumentUrl()).proofOfResidenceUrl(user.getProofOfResidenceUrl())
                .createdAt(user.getCreatedAt()).mustChangePassword(user.isMustChangePassword())
                .assignedZone(user.getAssignedZone()).build();
    }
}
