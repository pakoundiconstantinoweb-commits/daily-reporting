package com.itcinnovation.backend;

import java.time.Instant;
import java.util.Map;

import org.springframework.http.HttpStatus;
import org.springframework.http.HttpStatusCode;

public record ApiErrorResponse(
        Instant timestamp,
        int status,
        String error,
        String message,
        String path,
        Map<String, String> fieldErrors) {

    public static ApiErrorResponse of(
            HttpStatusCode status,
            String message,
            String path,
            Map<String, String> fieldErrors) {
        HttpStatus resolvedStatus = HttpStatus.resolve(status.value());
        return new ApiErrorResponse(
                Instant.now(),
                status.value(),
                resolvedStatus == null ? status.toString() : resolvedStatus.getReasonPhrase(),
                message,
                path,
                fieldErrors == null ? Map.of() : Map.copyOf(fieldErrors));
    }
}