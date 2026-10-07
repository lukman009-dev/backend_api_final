package com.shehia_management.api.auth;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ChangePasswordRequest {

    private String zanId;
    private String currentPassword;
    private String newPassword;
}