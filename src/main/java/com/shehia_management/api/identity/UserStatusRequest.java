package com.shehia_management.api.identity;

import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class UserStatusRequest {
    @NotNull
    private UserStatus status;
}
