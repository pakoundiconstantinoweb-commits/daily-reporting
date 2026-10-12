package com.itcinnovation.backend.reporting;

import jakarta.validation.constraints.NotNull;

public record ValidationRequest(@NotNull Boolean validated) {
}
