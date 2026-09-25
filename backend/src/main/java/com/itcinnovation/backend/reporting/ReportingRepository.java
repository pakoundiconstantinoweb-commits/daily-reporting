package com.itcinnovation.backend.reporting;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

public interface ReportingRepository extends JpaRepository<Reporting, Long> {
    Optional<Reporting> findByIdAndEmployeeId(Long id, Long employeeId);
    List<Reporting> findByEmployeeIdOrderByReportDateDesc(Long employeeId);
    List<Reporting> findByReportDateOrderByReportDateDesc(LocalDate reportDate);
}