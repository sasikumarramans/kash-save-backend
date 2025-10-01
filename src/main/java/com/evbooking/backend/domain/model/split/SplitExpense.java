package com.evbooking.backend.domain.model.split;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public class SplitExpense {
    private Long id;
    private String description;
    private BigDecimal totalAmount;
    private String currency;
    private Long paidByUserId;
    private Long groupId;
    private SplitType splitType;
    private Long createdByUserId;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    public SplitExpense() {}

    public SplitExpense(String description, BigDecimal totalAmount, String currency,
                       Long paidByUserId, Long groupId, SplitType splitType, Long createdByUserId) {
        this.description = description;
        this.totalAmount = totalAmount;
        this.currency = currency != null ? currency : "INR";
        this.paidByUserId = paidByUserId;
        this.groupId = groupId;
        this.splitType = splitType != null ? splitType : SplitType.EQUAL;
        this.createdByUserId = createdByUserId;
        this.createdAt = LocalDateTime.now();
        this.updatedAt = LocalDateTime.now();
    }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getDescription() { return description; }
    public void setDescription(String description) {
        this.description = description;
        this.updatedAt = LocalDateTime.now();
    }

    public BigDecimal getTotalAmount() { return totalAmount; }
    public void setTotalAmount(BigDecimal totalAmount) {
        this.totalAmount = totalAmount;
        this.updatedAt = LocalDateTime.now();
    }

    public String getCurrency() { return currency; }
    public void setCurrency(String currency) {
        this.currency = currency;
        this.updatedAt = LocalDateTime.now();
    }

    public Long getPaidByUserId() { return paidByUserId; }
    public void setPaidByUserId(Long paidByUserId) {
        this.paidByUserId = paidByUserId;
        this.updatedAt = LocalDateTime.now();
    }

    public Long getGroupId() { return groupId; }
    public void setGroupId(Long groupId) { this.groupId = groupId; }

    public SplitType getSplitType() { return splitType; }
    public void setSplitType(SplitType splitType) {
        this.splitType = splitType;
        this.updatedAt = LocalDateTime.now();
    }

    public Long getCreatedByUserId() { return createdByUserId; }
    public void setCreatedByUserId(Long createdByUserId) { this.createdByUserId = createdByUserId; }

    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }

    public LocalDateTime getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(LocalDateTime updatedAt) { this.updatedAt = updatedAt; }

    public boolean isGroupSplit() {
        return groupId != null;
    }

    public boolean isIndividualSplit() {
        return groupId == null;
    }
}