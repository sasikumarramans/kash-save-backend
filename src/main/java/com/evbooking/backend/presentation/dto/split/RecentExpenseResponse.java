package com.evbooking.backend.presentation.dto.split;

import com.fasterxml.jackson.annotation.JsonFormat;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public class RecentExpenseResponse {
    private Long id;
    private String description;
    private BigDecimal totalAmount;
    private String currency;
    private String paidByUsername;
    private String paidByUserId;

    @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss")
    private LocalDateTime createdAt;

    public RecentExpenseResponse() {}

    public RecentExpenseResponse(Long id, String description, BigDecimal totalAmount, String currency,
                                String paidByUsername, String paidByUserId, LocalDateTime createdAt) {
        this.id = id;
        this.description = description;
        this.totalAmount = totalAmount;
        this.currency = currency;
        this.paidByUsername = paidByUsername;
        this.paidByUserId = paidByUserId;
        this.createdAt = createdAt;
    }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }

    public BigDecimal getTotalAmount() { return totalAmount; }
    public void setTotalAmount(BigDecimal totalAmount) { this.totalAmount = totalAmount; }

    public String getCurrency() { return currency; }
    public void setCurrency(String currency) { this.currency = currency; }

    public String getPaidByUsername() { return paidByUsername; }
    public void setPaidByUsername(String paidByUsername) { this.paidByUsername = paidByUsername; }

    public String getPaidByUserId() { return paidByUserId; }
    public void setPaidByUserId(String paidByUserId) { this.paidByUserId = paidByUserId; }

    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }
}