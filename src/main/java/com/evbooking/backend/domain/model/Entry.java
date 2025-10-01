package com.evbooking.backend.domain.model;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public class Entry {
    private Long id;
    private Long bookId;
    private EntryType type;
    private String name;
    private BigDecimal amount;
    private String currency;
    private LocalDateTime dateTime;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    public Entry() {}

    public Entry(Long bookId, EntryType type, String name, BigDecimal amount, String currency, LocalDateTime dateTime) {
        this.bookId = bookId;
        this.type = type;
        this.name = name;
        this.amount = amount;
        this.currency = currency != null ? currency : "INR";
        this.dateTime = dateTime;
        this.createdAt = LocalDateTime.now();
        this.updatedAt = LocalDateTime.now();
    }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public Long getBookId() { return bookId; }
    public void setBookId(Long bookId) { this.bookId = bookId; }

    public EntryType getType() { return type; }
    public void setType(EntryType type) { this.type = type; }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public BigDecimal getAmount() { return amount; }
    public void setAmount(BigDecimal amount) { this.amount = amount; }

    public String getCurrency() { return currency; }
    public void setCurrency(String currency) { this.currency = currency; }

    public LocalDateTime getDateTime() { return dateTime; }
    public void setDateTime(LocalDateTime dateTime) { this.dateTime = dateTime; }

    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }

    public LocalDateTime getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(LocalDateTime updatedAt) { this.updatedAt = updatedAt; }

    public boolean isExpense() {
        return type == EntryType.EXPENSE;
    }

    public boolean isIncome() {
        return type == EntryType.INCOME;
    }
}