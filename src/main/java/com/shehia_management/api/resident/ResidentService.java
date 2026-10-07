package com.shehia_management.api.resident;

import com.shehia_management.api.identity.UserResponse;
import com.shehia_management.api.identity.UserStatus;

import java.util.List;

/**
 * Owns every business rule for managing a resident account, no matter which
 * front door triggers it: a resident registering themselves, an admin
 * managing the full resident roster, or a staff member managing only the
 * residents in their assigned zone. Keeping all of it here (instead of
 * splitting the zone-scoped variants into the staff capability) means a bug
 * in "how a resident's status gets updated" only ever has one place to look,
 * regardless of who called it.
 */
public interface ResidentService {

    UserResponse registerResident(RegisterRequest request);
    UserResponse getResident(Long id);
    UserResponse getCurrentResident(String zanId);
    UserResponse updateResidentStatus(Long id, UserStatus status);
    List<UserResponse> getAllResidents(UserStatus status);

    // Anonymous counts for the public landing page (ACTIVE residents only).
    PublicStatsResponse getPublicStats();

    // Zone-scoped variants used by staff dashboards - a staff member may
    // only see/manage residents whose house number falls in their zone.
    UserResponse registerResidentInZone(RegisterRequest request, String zone);
    List<UserResponse> getResidentsInZone(String zone, UserStatus status);
    UserResponse getResidentInZone(Long id, String zone);
    UserResponse updateResidentStatusInZone(Long id, UserStatus status, String zone);
}
