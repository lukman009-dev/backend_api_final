package com.shehia_management.api.auth;

import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
public class LoginResponse {

    private String token;
    private String zanId;
    private String role;
    private String tokenType = "Bearer";
    private boolean mustChangePassword;

    public LoginResponse(
            String token,
            String zanId,
            String role,
            boolean mustChangePassword
    ) {
        this.token = token;
        this.zanId = zanId;
        this.role = role;
        this.mustChangePassword = mustChangePassword;
        this.tokenType = "Bearer";
    }

    public LoginResponse(
            String token,
            String zanId,
            String role,
            boolean mustChangePassword,
            String tokenType
    ) {
        this.token = token;
        this.zanId = zanId;
        this.role = role;
        this.mustChangePassword = mustChangePassword;
        this.tokenType = tokenType;
    }
}