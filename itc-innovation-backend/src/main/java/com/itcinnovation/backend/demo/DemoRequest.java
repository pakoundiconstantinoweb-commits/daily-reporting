package com.itcinnovation.backend.demo;

import java.time.Instant;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

@Entity
@Table(name = "demo_requests")
public class DemoRequest {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 160)
    private String fullName;

    @Column(nullable = false, length = 160)
    private String email;

    @Column(nullable = false, length = 30)
    private String phone;

    @Column(nullable = false)
    private Instant createdAt;

    @Column(nullable = false)
    private Instant consentedAt;

    @Column(nullable = false)
    private Instant demoExpiresAt;

    @Column
    private Long demoManagerId;

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public String getFullName() { return fullName; }
    public void setFullName(String fullName) { this.fullName = fullName; }
    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }
    public String getPhone() { return phone; }
    public void setPhone(String phone) { this.phone = phone; }
    public Instant getCreatedAt() { return createdAt; }
    public void setCreatedAt(Instant createdAt) { this.createdAt = createdAt; }
    public Instant getConsentedAt() { return consentedAt; }
    public void setConsentedAt(Instant consentedAt) { this.consentedAt = consentedAt; }
    public Instant getDemoExpiresAt() { return demoExpiresAt; }
    public void setDemoExpiresAt(Instant demoExpiresAt) { this.demoExpiresAt = demoExpiresAt; }
    public Long getDemoManagerId() { return demoManagerId; }
    public void setDemoManagerId(Long demoManagerId) { this.demoManagerId = demoManagerId; }
}