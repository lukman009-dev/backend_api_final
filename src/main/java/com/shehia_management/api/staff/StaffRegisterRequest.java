package com.shehia_management.api.staff;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;

/**
 * First-login self-registration payload for staff accounts.
 *
 * Staff only supply a username and password (no personal/resident-style
 * details). The account is created with role ROLE_STAFF and status PENDING;
 * an admin must assign it a zone and approve (activate) it before the
 * staff member can sign in.
 */
@Data
public class StaffRegisterRequest {

    @NotBlank
    private String username;

    @NotBlank
    @Size(min = 6, message = "Password must be at least 6 characters")
    private String password;
}
