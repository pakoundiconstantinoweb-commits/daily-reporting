package com.itcinnovation.backend.reporting;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record ReportingRequest(
        @NotBlank @Size(max = 160) String title,
        @NotBlank @Size(max = 5000) String description
) {
}