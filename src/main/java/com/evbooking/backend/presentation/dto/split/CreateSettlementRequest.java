package com.evbooking.backend.presentation.dto.split;

import jakarta.validation.constraints.*;

import java.math.BigDecimal;

public class CreateSettlementRequest {

    @NotBlank(message = "From username is required")
    private String fromUsername;

    @NotBlank(message = "To username is required")
    private String toUsername;

    @NotNull(message = "Amount is required")
    @DecimalMin(value = "0.01", message = "Amount must be greater than 0")
    @Digits(integer = 10, fraction = 2, message = "Amount must have at most 2 decimal places")
    private BigDecimal amount;

    @Size(max = 3, message = "Currency code must be 3 characters")
    private String currency = "INR";

    private Long groupId; // Optional - for group settlements

    @Size(max = 500, message = "Notes cannot exceed 500 characters")
    private String notes;

    public CreateSettlementRequest() {}

    public CreateSettlementRequest(String fromUsername, String toUsername, BigDecimal amount,
                                  String currency, Long groupId, String notes) {
        this.fromUsername = fromUsername;
        this.toUsername = toUsername;
        this.amount = amount;
        this.currency = currency != null ? currency : "INR";
        this.groupId = groupId;
        this.notes = notes;
    }

    // Getters and Setters
    public String getFromUsername() { return fromUsername; }
    public void setFromUsername(String fromUsername) { this.fromUsername = fromUsername; }

    public String getToUsername() { return toUsername; }
    public void setToUsername(String toUsername) { this.toUsername = toUsername; }

    public BigDecimal getAmount() { return amount; }
    public void setAmount(BigDecimal amount) { this.amount = amount; }

    public String getCurrency() { return currency; }
    public void setCurrency(String currency) { this.currency = currency; }

    public Long getGroupId() { return groupId; }
    public void setGroupId(Long groupId) { this.groupId = groupId; }

    public String getNotes() { return notes; }
    public void setNotes(String notes) { this.notes = notes; }
}
