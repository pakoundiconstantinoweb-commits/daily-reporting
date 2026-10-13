package com.itcinnovation.backend.demo;

import jakarta.validation.constraints.AssertTrue;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

public record DemoRequestForm(
        @NotBlank @Size(max = 160) String fullName,
        @NotBlank @Email @Size(max = 160) String email,
        @NotBlank @Pattern(regexp = "\\+?[0-9 ()().-]{7,30}") String phone,
        @NotNull @AssertTrue Boolean consentGiven
) {
}