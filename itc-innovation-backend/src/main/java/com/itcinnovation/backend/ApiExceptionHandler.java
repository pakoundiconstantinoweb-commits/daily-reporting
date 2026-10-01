package com.itcinnovation.backend;

import java.util.LinkedHashMap;
import java.util.Map;

import jakarta.servlet.http.HttpServletRequest;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.web.HttpRequestMethodNotSupportedException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.servlet.resource.NoResourceFoundException;
import org.springframework.web.server.ResponseStatusException;

@RestControllerAdvice
public class ApiExceptionHandler {
        private static final Logger logger = LoggerFactory.getLogger(ApiExceptionHandler.class);

    @ExceptionHandler(ResponseStatusException.class)
    ResponseEntity<ApiErrorResponse> handleResponseStatus(
            ResponseStatusException exception,
            HttpServletRequest request) {
        String message = exception.getReason() == null
                ? "La requête n’a pas pu être traitée."
                : exception.getReason();
        return ResponseEntity.status(exception.getStatusCode()).body(ApiErrorResponse.of(
                exception.getStatusCode(), message, request.getRequestURI(), Map.of()));
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    ResponseEntity<ApiErrorResponse> handleValidation(
            MethodArgumentNotValidException exception,
            HttpServletRequest request) {
        Map<String, String> fieldErrors = new LinkedHashMap<>();
        exception.getBindingResult().getFieldErrors().forEach(fieldError ->
                fieldErrors.merge(
                        fieldError.getField(),
                        validationMessage(fieldError.getCode()),
                        (existing, next) -> existing + " " + next));

        return ResponseEntity.badRequest().body(ApiErrorResponse.of(
                HttpStatus.BAD_REQUEST,
                "Certains champs sont invalides.",
                request.getRequestURI(),
                fieldErrors));
    }

        @ExceptionHandler(NoResourceFoundException.class)
        ResponseEntity<ApiErrorResponse> handleNotFound(HttpServletRequest request) {
                return ResponseEntity.status(HttpStatus.NOT_FOUND).body(ApiErrorResponse.of(
                                HttpStatus.NOT_FOUND,
                                "La ressource demandée est introuvable.",
                                request.getRequestURI(),
                                Map.of()));
        }

        @ExceptionHandler(HttpRequestMethodNotSupportedException.class)
        ResponseEntity<ApiErrorResponse> handleMethodNotAllowed(HttpServletRequest request) {
                return ResponseEntity.status(HttpStatus.METHOD_NOT_ALLOWED).body(ApiErrorResponse.of(
                                HttpStatus.METHOD_NOT_ALLOWED,
                                "Cette méthode HTTP n’est pas autorisée pour cette ressource.",
                                request.getRequestURI(),
                                Map.of()));
        }

    @ExceptionHandler(HttpMessageNotReadableException.class)
    ResponseEntity<ApiErrorResponse> handleUnreadableRequest(HttpServletRequest request) {
        return ResponseEntity.badRequest().body(ApiErrorResponse.of(
                HttpStatus.BAD_REQUEST,
                "Le contenu de la requête est invalide.",
                request.getRequestURI(),
                Map.of()));
    }

    @ExceptionHandler(Exception.class)
        ResponseEntity<ApiErrorResponse> handleUnexpected(Exception exception, HttpServletRequest request) {
                logger.error("Unexpected API error for {} {}", request.getMethod(), request.getRequestURI(), exception);
        return ResponseEntity.internalServerError().body(ApiErrorResponse.of(
                HttpStatus.INTERNAL_SERVER_ERROR,
                "Une erreur interne est survenue. Réessayez plus tard.",
                request.getRequestURI(),
                Map.of()));
    }

        private String validationMessage(String code) {
                if (code == null) return "Valeur invalide.";
                return switch (code) {
                        case "NotBlank", "NotNull", "NotEmpty" -> "Ce champ est obligatoire.";
                        case "Email" -> "L’adresse e-mail n’est pas valide.";
                        case "Size" -> "La longueur de ce champ n’est pas valide.";
                        case "Min", "Max", "Positive", "PositiveOrZero", "Negative", "NegativeOrZero" ->
                                        "La valeur de ce champ n’est pas valide.";
                        default -> "Valeur invalide.";
                };
        }
}