package com.itcinnovation.backend.demo;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.List;

import org.junit.jupiter.api.Test;

import com.itcinnovation.backend.reporting.ReportingRepository;
import com.itcinnovation.backend.user.UserRepository;
import com.itcinnovation.backend.user.UserRole;

class DemoRequestCleanupServiceTest {

    @Test
    void deletesOnlyExpiredDemoWorkspaceAndRetainsContactRequest() {
        DemoRequestRepository requestRepository = mock(DemoRequestRepository.class);
        ReportingRepository reportingRepository = mock(ReportingRepository.class);
        UserRepository userRepository = mock(UserRepository.class);
        when(userRepository.findExpiredDemoManagerIds(org.mockito.ArgumentMatchers.any()))
                .thenReturn(List.of(42L));
        DemoRequestCleanupService service = new DemoRequestCleanupService(
                requestRepository, reportingRepository, userRepository);

        service.removeExpiredDemoAccounts();

        verify(reportingRepository).deleteByEmployee_Manager_Id(42L);
        verify(userRepository).deleteByManager_IdAndRole(42L, UserRole.EMPLOYEE);
        verify(requestRepository).clearDemoManagerId(42L);
        verify(userRepository).deleteById(42L);
        verify(requestRepository, org.mockito.Mockito.never())
                .delete(org.mockito.ArgumentMatchers.any(DemoRequest.class));
    }
}
