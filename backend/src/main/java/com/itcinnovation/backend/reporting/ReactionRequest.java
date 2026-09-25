package com.itcinnovation.backend.reporting;

import jakarta.validation.constraints.NotNull;

public record ReactionRequest(@NotNull ReportReaction reaction) {
}