package com.itcinnovation.backend.reporting;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.time.LocalDate;

import org.junit.jupiter.api.Test;

import com.itcinnovation.backend.user.User;

class ReportingDecisionTest {

    @Test
    void reportsExplicitManagerDecisionToEmployeeAndManager() {
        Reporting reporting = reporting();
        reporting.setManagerDecision(ManagerDecision.REJECTED);
        reporting.setValidated(false);

        ReportingResponse response = ReportingResponse.from(reporting);

        assertEquals(ManagerDecision.REJECTED, response.decision());
        assertFalse(response.validated());
    }

    @Test
    void mapsPreviouslyValidatedReportsWithoutLosingTheirState() {
        Reporting reporting = reporting();
        reporting.setManagerDecision(null);
        reporting.setValidated(true);

        ReportingResponse response = ReportingResponse.from(reporting);

        assertEquals(ManagerDecision.VALIDATED, response.decision());
        assertTrue(response.validated());
    }

    @Test
    void mapsLegacyDislikeToRejected() {
        Reporting reporting = reporting();
        reporting.setManagerDecision(null);
        reporting.setReaction(ReportReaction.DISLIKE);

        ReportingResponse response = ReportingResponse.from(reporting);

        assertEquals(ManagerDecision.REJECTED, response.decision());
        assertFalse(response.validated());
    }

    private Reporting reporting() {
        User employee = new User();
        employee.setId(1L);
        employee.setFirstName("Awa");
        employee.setLastName("Mensah");

        Reporting reporting = new Reporting();
        reporting.setId(10L);
        reporting.setReportDate(LocalDate.of(2026, 10, 12));
        reporting.setTitle("Rapport du jour");
        reporting.setDescription("Travail effectué");
        reporting.setStatus(ReportingStatus.SUBMITTED);
        reporting.setEmployee(employee);
        return reporting;
    }
}
