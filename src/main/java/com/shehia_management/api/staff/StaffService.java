package com.shehia_management.api.staff;

import com.shehia_management.api.identity.UserResponse;
import com.shehia_management.api.identity.UserStatus;

import java.util.List;

/**
 * Owns the staff *account lifecycle*: self-registration, profile, admin
 * approval and zone assignment. It deliberately does NOT own "manage
 * residents/issues in my zone" - those are resident/issue business rules
 * (see ResidentService.*InZone / IssueService.*InZone); this service only
 * owns facts about the staff account itself.
 */
public interface StaffService {

    UserResponse registerStaff(StaffRegisterRequest request);
    UserResponse getCurrentStaff(String zanId);
    UserResponse getStaffById(Long id);
    List<UserResponse> getAllStaff(UserStatus status);
    UserResponse updateStaffStatus(Long id, UserStatus status);
    UserResponse assignStaffZone(Long id, String zone);

    /**
     * Resolves the assigned zone for the given staff account, or throws if
     * none has been assigned yet. EXTRACTED SHARED LOGIC: this was a
     * private zoneOf(Authentication) helper copy-pasted across StaffController,
     * and would otherwise have been re-copied into every zone-scoped staff
     * controller (residents, issues, dashboard). "A staff account needs a
     * zone to act" is a staff-account business rule, so it lives here once,
     * and every staff-facing controller just calls it.
     */
    String requireAssignedZone(String zanId);
}
