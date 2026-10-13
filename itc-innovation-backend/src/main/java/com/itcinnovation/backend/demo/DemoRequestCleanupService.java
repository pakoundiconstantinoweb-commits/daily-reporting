package com.itcinnovation.backend.demo;

import java.time.Instant;

import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.itcinnovation.backend.reporting.ReportingRepository;
import com.itcinnovation.backend.user.UserRepository;
import com.itcinnovation.backend.user.UserRole;

@Service
public class DemoRequestCleanupService {

    private final DemoRequestRepository demoRequestRepository;
    private final ReportingRepository reportingRepository;
    private final UserRepository userRepository;

    public DemoRequestCleanupService(
            DemoRequestRepository demoRequestRepository,
            ReportingRepository reportingRepository,
            UserRepository userRepository) {
        this.demoRequestRepository = demoRequestRepository;
        this.reportingRepository = reportingRepository;
        this.userRepository = userRepository;
    }

    @Scheduled(fixedDelay = 300_000)
    @Transactional
    public void removeExpiredDemoAccounts() {
        for (Long managerId : userRepository.findExpiredDemoManagerIds(Instant.now())) {
            reportingRepository.deleteByEmployee_Manager_Id(managerId);
            userRepository.deleteByManager_IdAndRole(managerId, UserRole.EMPLOYEE);
            demoRequestRepository.clearDemoManagerId(managerId);
            userRepository.deleteById(managerId);
        }
    }
}
