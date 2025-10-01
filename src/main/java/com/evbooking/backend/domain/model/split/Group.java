package com.evbooking.backend.domain.model.split;

import java.time.LocalDateTime;

public class Group {
    private Long id;
    private String name;
    private String description;
    private Long adminUserId;
    private String currency;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    public Group() {}

    public Group(String name, String description, Long adminUserId, String currency) {
        this.name = name;
        this.description = description;
        this.adminUserId = adminUserId;
        this.currency = currency != null ? currency : "INR";
        this.createdAt = LocalDateTime.now();
        this.updatedAt = LocalDateTime.now();
    }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getName() { return name; }
    public void setName(String name) {
        this.name = name;
        this.updatedAt = LocalDateTime.now();
    }

    public String getDescription() { return description; }
    public void setDescription(String description) {
        this.description = description;
        this.updatedAt = LocalDateTime.now();
    }

    public Long getAdminUserId() { return adminUserId; }
    public void setAdminUserId(Long adminUserId) {
        this.adminUserId = adminUserId;
        this.updatedAt = LocalDateTime.now();
    }

    public String getCurrency() { return currency; }
    public void setCurrency(String currency) {
        this.currency = currency;
        this.updatedAt = LocalDateTime.now();
    }

    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }

    public LocalDateTime getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(LocalDateTime updatedAt) { this.updatedAt = updatedAt; }
}