package com.itcinnovation.backend.demo;

import java.io.IOException;
import java.time.Instant;

import org.springframework.http.HttpStatus;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.itcinnovation.backend.ApiErrorResponse;
import com.itcinnovation.backend.user.UserRepository;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

@Component
public class DemoTrialFilter extends OncePerRequestFilter {

    private final UserRepository userRepository;
    private final ObjectMapper objectMapper;

    public DemoTrialFilter(UserRepository userRepository, ObjectMapper objectMapper) {
        this.userRepository = userRepository;
        this.objectMapper = objectMapper;
    }

    @Override
    protected void doFilterInternal(
            HttpServletRequest request,
            HttpServletResponse response,
            FilterChain filterChain) throws ServletException, IOException {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication != null && authentication.isAuthenticated()) {
            String principal = authentication.getName();
            if (principal != null && principal.matches("\\d+")) {
                Instant expiresAt = userRepository.findDemoExpiresAtForUser(Long.valueOf(principal)).orElse(null);
                if (expiresAt != null && !expiresAt.isAfter(Instant.now())) {
                    response.setStatus(HttpStatus.FORBIDDEN.value());
                    response.setCharacterEncoding("UTF-8");
                    response.setContentType("application/json");
                    objectMapper.writeValue(response.getOutputStream(), ApiErrorResponse.of(
                            HttpStatus.FORBIDDEN,
                            "Votre période de démonstration est terminée. Contactez-nous pour continuer.",
                            request.getRequestURI(),
                            null));
                    return;
                }
            }
        }
        filterChain.doFilter(request, response);
    }
}
