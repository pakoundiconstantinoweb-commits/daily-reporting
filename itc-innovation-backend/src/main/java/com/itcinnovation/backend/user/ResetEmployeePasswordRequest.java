package com.itcinnovation.backend.user;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record ResetEmployeePasswordRequest(
        @NotBlank @Size(min = 8, max = 120) String newPassword
) {
}