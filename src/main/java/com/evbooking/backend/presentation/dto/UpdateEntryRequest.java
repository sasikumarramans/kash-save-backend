package com.evbooking.backend.presentation.dto;

import com.evbooking.backend.domain.model.EntryType;
import com.fasterxml.jackson.annotation.JsonFormat;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Size;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public class UpdateEntryRequest {

    private EntryType type;

    @Size(max = 200, message = "Entry name must not exceed 200 characters")
    private String name;

    @DecimalMin(value = "0.01", message = "Amount must be greater than zero")
    private BigDecimal amount;

    @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss")
    private LocalDateTime dateTime;

    public UpdateEntryRequest() {}

    public UpdateEntryRequest(EntryType type, String name, BigDecimal amount, LocalDateTime dateTime) {
        this.type = type;
        this.name = name;
        this.amount = amount;
        this.dateTime = dateTime;
    }

    public EntryType getType() { return type; }
    public void setType(EntryType type) { this.type = type; }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public BigDecimal getAmount() { return amount; }
    public void setAmount(BigDecimal amount) { this.amount = amount; }

    public LocalDateTime getDateTime() { return dateTime; }
    public void setDateTime(LocalDateTime dateTime) { this.dateTime = dateTime; }
}