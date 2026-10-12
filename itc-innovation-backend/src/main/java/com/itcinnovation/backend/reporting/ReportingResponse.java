package com.itcinnovation.backend.reporting;

import java.time.LocalDate;

public record ReportingResponse(
        Long id,
        LocalDate reportDate,
        String title,
        String description,
        ReportingStatus status,
        boolean validated,
        ManagerDecision decision,
        Long employeeId,
        String employeeFirstName,
        String employeeLastName
) {
    static ReportingResponse from(Reporting reporting) {
        ManagerDecision decision = reporting.getManagerDecision();
        if (decision == null) {
            decision = Boolean.TRUE.equals(reporting.getValidated())
                    || reporting.getReaction() == ReportReaction.LIKE
                    ? ManagerDecision.VALIDATED
                    : reporting.getReaction() == ReportReaction.DISLIKE
                            ? ManagerDecision.REJECTED
                            : ManagerDecision.PENDING;
        }
        return new ReportingResponse(
                reporting.getId(),
                reporting.getReportDate(),
                reporting.getTitle(),
                reporting.getDescription(),
                reporting.getStatus(),
                decision == ManagerDecision.VALIDATED,
                decision,
                reporting.getEmployee().getId(),
                reporting.getEmployee().getFirstName(),
                reporting.getEmployee().getLastName());
    }
}