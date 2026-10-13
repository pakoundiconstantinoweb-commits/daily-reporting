package com.itcinnovation.backend.demo;

import java.time.Instant;

public record DemoCredentialsResponse(String email, String password, Instant demoExpiresAt) {
}
