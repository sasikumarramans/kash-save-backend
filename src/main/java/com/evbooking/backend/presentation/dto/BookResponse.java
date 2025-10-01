package com.evbooking.backend.presentation.dto;

import com.fasterxml.jackson.annotation.JsonFormat;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public class BookResponse {
    private Long id;
    private String name;
    private String description;
    private String currency;

    @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss")
    private LocalDateTime createdAt;

    @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss")
    private LocalDateTime updatedAt;

    private BigDecimal totalExpense;
    private BigDecimal totalIncome;

    @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss")
    private LocalDateTime lastEntryDateTime;

    public BookResponse() {}

    public BookResponse(Long id, String name, String description, String currency, LocalDateTime createdAt, LocalDateTime updatedAt) {
        this.id = id;
        this.name = name;
        this.description = description;
        this.currency = currency;
        this.createdAt = createdAt;
        this.updatedAt = updatedAt;
    }

    public BookResponse(Long id, String name, String description, String currency, LocalDateTime createdAt, LocalDateTime updatedAt,
                       BigDecimal totalExpense, BigDecimal totalIncome, LocalDateTime lastEntryDateTime) {
        this.id = id;
        this.name = name;
        this.description = description;
        this.currency = currency;
        this.createdAt = createdAt;
        this.updatedAt = updatedAt;
        this.totalExpense = totalExpense;
        this.totalIncome = totalIncome;
        this.lastEntryDateTime = lastEntryDateTime;
    }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }

    public String getCurrency() { return currency; }
    public void setCurrency(String currency) { this.currency = currency; }

    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }

    public LocalDateTime getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(LocalDateTime updatedAt) { this.updatedAt = updatedAt; }

    public BigDecimal getTotalExpense() { return totalExpense; }
    public void setTotalExpense(BigDecimal totalExpense) { this.totalExpense = totalExpense; }

    public BigDecimal getTotalIncome() { return totalIncome; }
    public void setTotalIncome(BigDecimal totalIncome) { this.totalIncome = totalIncome; }

    public LocalDateTime getLastEntryDateTime() { return lastEntryDateTime; }
    public void setLastEntryDateTime(LocalDateTime lastEntryDateTime) { this.lastEntryDateTime = lastEntryDateTime; }
}