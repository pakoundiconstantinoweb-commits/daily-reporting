package com.itcinnovation.backend.reporting;

import jakarta.validation.constraints.NotNull;

public record DecisionRequest(@NotNull ManagerDecision decision) {
}
