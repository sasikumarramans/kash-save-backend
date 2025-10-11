package com.evbooking.backend.domain.model;

import java.time.LocalDateTime;

public class Book {
    private Long id;
    private String name;
    private String description;
    private String currency;
    private String userId;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    public Book() {}

    public Book(String name, String description, String currency, String userId) {
        this.name = name;
        this.description = description;
        this.currency = currency != null ? currency : "INR";
        this.userId = userId;
        this.createdAt = LocalDateTime.now();
        this.updatedAt = LocalDateTime.now();
    }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }

    public String getCurrency() { return currency; }
    public void setCurrency(String currency) { this.currency = currency; }

    public String getUserId() { return userId; }
    public void setUserId(String userId) { this.userId = userId; }

    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }

    public LocalDateTime getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(LocalDateTime updatedAt) { this.updatedAt = updatedAt; }
}