package com.itcinnovation.backend.auth;

import static org.junit.jupiter.api.Assertions.assertFalse;

import java.util.Map;

import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.itcinnovation.backend.ApiErrorResponse;

class SecurityConfigTest {

    @Test
    void objectMapperSerializesInstantInApiErrors() throws Exception {
        ObjectMapper objectMapper = new SecurityConfig().objectMapper();
        String json = objectMapper.writeValueAsString(ApiErrorResponse.of(
                HttpStatus.UNAUTHORIZED, "Authentication required", "/api/reports/mine", Map.of()));
        JsonNode response = objectMapper.readTree(json);

        assertFalse(response.get("timestamp").asText().isBlank());
    }
}
