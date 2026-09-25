package com.itcinnovation.backend.reporting;

import java.time.LocalDate;
import java.util.List;

import org.springframework.http.HttpStatus;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;

import com.itcinnovation.backend.user.User;
import com.itcinnovation.backend.user.UserRepository;

import jakarta.validation.Valid;

@RestController
@RequestMapping("/api/reports")
public class ReportingController {

    private final ReportingRepository reportingRepository;
    private final UserRepository userRepository;

    public ReportingController(ReportingRepository reportingRepository, UserRepository userRepository) {
        this.reportingRepository = reportingRepository;
        this.userRepository = userRepository;
    }

    @GetMapping("/mine")
    public List<ReportingResponse> mine(Authentication authentication) {
        Long employeeId = currentUserId(authentication);
        return reportingRepository.findByEmployeeIdOrderByReportDateDesc(employeeId)
                .stream().map(ReportingResponse::from).toList();
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public ReportingResponse saveDraft(
            @Valid @RequestBody ReportingRequest request,
            Authentication authentication) {
        Reporting reporting = new Reporting();
        reporting.setReportDate(LocalDate.now());
        reporting.setTitle(request.title());
        reporting.setDescription(request.description());
        reporting.setStatus(ReportingStatus.DRAFT);
        reporting.setEmployee(currentUser(authentication));
        return ReportingResponse.from(reportingRepository.save(reporting));
    }

    @PutMapping("/{id}")
    public ReportingResponse updateDraft(
            @PathVariable Long id,
            @Valid @RequestBody ReportingRequest request,
            Authentication authentication) {
        Reporting reporting = employeeReporting(id, authentication);
        if (reporting.getStatus() != ReportingStatus.DRAFT) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Un reporting envoyé ne peut plus être modifié");
        }
        reporting.setTitle(request.title());
        reporting.setDescription(request.description());
        return ReportingResponse.from(reportingRepository.save(reporting));
    }

    @PostMapping("/{id}/submit")
    public ReportingResponse submit(@PathVariable Long id, Authentication authentication) {
        Reporting reporting = employeeReporting(id, authentication);
        reporting.setStatus(ReportingStatus.SUBMITTED);
        return ReportingResponse.from(reportingRepository.save(reporting));
    }

    @GetMapping("/manager")
    public List<ReportingResponse> all(@RequestParam(required = false) LocalDate date) {
        List<Reporting> reports = date == null
                ? reportingRepository.findAll()
                : reportingRepository.findByReportDateOrderByReportDateDesc(date);
        return reports.stream()
                .filter(report -> report.getStatus() == ReportingStatus.SUBMITTED)
                .map(ReportingResponse::from).toList();
    }

    @PatchMapping("/manager/{id}/reaction")
    public ReportingResponse react(
            @PathVariable Long id,
            @Valid @RequestBody ReactionRequest request) {
        Reporting reporting = reportingRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Reporting introuvable"));
        reporting.setReaction(request.reaction());
        return ReportingResponse.from(reportingRepository.save(reporting));
    }

    private Reporting employeeReporting(Long id, Authentication authentication) {
        return reportingRepository.findByIdAndEmployeeId(id, currentUserId(authentication))
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Reporting introuvable"));
    }

    private User currentUser(Authentication authentication) {
        return userRepository.findById(currentUserId(authentication))
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Utilisateur introuvable"));
    }

    private Long currentUserId(Authentication authentication) {
        try {
            return Long.valueOf(authentication.getName());
        } catch (NumberFormatException exception) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Identifiant utilisateur invalide");
        }
    }
}