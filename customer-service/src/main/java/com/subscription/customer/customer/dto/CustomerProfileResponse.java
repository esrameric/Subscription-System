package com.subscription.customer.customer.dto;

import java.time.Instant;

public class CustomerProfileResponse {
    private Long id;
    private String email;
    private String name;
    private String roles;
    private String status;
    private Instant createdAt;

    public CustomerProfileResponse(Long id, String email, String name, String roles, String status, Instant createdAt) {
        this.id = id;
        this.email = email;
        this.name = name;
        this.roles = roles;
        this.status = status;
        this.createdAt = createdAt;
    }

    public Long getId() {
        return id;
    }

    public String getEmail() {
        return email;
    }

    public String getName() {
        return name;
    }

    public String getRoles() {
        return roles;
    }

    public String getStatus() {
        return status;
    }

    public Instant getCreatedAt() {
        return createdAt;
    }
}
