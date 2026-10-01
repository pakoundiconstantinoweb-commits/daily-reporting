package com.itcinnovation.backend.reporting;

import java.time.LocalDate;

public record ReportingResponse(
        Long id,
        LocalDate reportDate,
        String title,
        String description,
        ReportingStatus status,
        ReportReaction reaction,
        Long employeeId,
        String employeeFirstName,
        String employeeLastName
) {
    static ReportingResponse from(Reporting reporting) {
        return new ReportingResponse(
                reporting.getId(),
                reporting.getReportDate(),
                reporting.getTitle(),
                reporting.getDescription(),
                reporting.getStatus(),
                reporting.getReaction(),
                reporting.getEmployee().getId(),
                reporting.getEmployee().getFirstName(),
                reporting.getEmployee().getLastName());
    }
}