package com.itcinnovation.backend.reporting;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

public interface ReportingRepository extends JpaRepository<Reporting, Long> {
    Optional<Reporting> findByIdAndEmployeeId(Long id, Long employeeId);
    List<Reporting> findByEmployeeIdOrderByReportDateDesc(Long employeeId);
    List<Reporting> findByEmployeeIdAndReportDateOrderByReportDateDesc(Long employeeId, LocalDate reportDate);
    List<Reporting> findByReportDateOrderByReportDateDesc(LocalDate reportDate);

        List<Reporting> findByEmployee_Manager_IdAndStatusOrderByReportDateDesc(
            Long managerId, ReportingStatus status);

        List<Reporting> findByEmployee_Manager_IdAndReportDateAndStatusOrderByReportDateDesc(
            Long managerId, LocalDate reportDate, ReportingStatus status);

        Optional<Reporting> findByIdAndEmployee_Manager_Id(Long id, Long managerId);
}