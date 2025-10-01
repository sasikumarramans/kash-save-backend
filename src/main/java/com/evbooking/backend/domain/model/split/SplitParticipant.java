package com.evbooking.backend.domain.model.split;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public class SplitParticipant {
    private Long id;
    private Long splitExpenseId;
    private Long userId;
    private BigDecimal amountOwed;
    private BigDecimal splitValue;
    private boolean isSettled;
    private LocalDateTime settledAt;
    private LocalDateTime createdAt;

    public SplitParticipant() {}

    public SplitParticipant(Long splitExpenseId, Long userId, BigDecimal amountOwed, BigDecimal splitValue) {
        this.splitExpenseId = splitExpenseId;
        this.userId = userId;
        this.amountOwed = amountOwed;
        this.splitValue = splitValue;
        this.isSettled = false;
        this.createdAt = LocalDateTime.now();
    }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public Long getSplitExpenseId() { return splitExpenseId; }
    public void setSplitExpenseId(Long splitExpenseId) { this.splitExpenseId = splitExpenseId; }

    public Long getUserId() { return userId; }
    public void setUserId(Long userId) { this.userId = userId; }

    public BigDecimal getAmountOwed() { return amountOwed; }
    public void setAmountOwed(BigDecimal amountOwed) { this.amountOwed = amountOwed; }

    public BigDecimal getSplitValue() { return splitValue; }
    public void setSplitValue(BigDecimal splitValue) { this.splitValue = splitValue; }

    public boolean isSettled() { return isSettled; }
    public void setSettled(boolean settled) {
        isSettled = settled;
        if (settled && settledAt == null) {
            settledAt = LocalDateTime.now();
        } else if (!settled) {
            settledAt = null;
        }
    }

    public LocalDateTime getSettledAt() { return settledAt; }
    public void setSettledAt(LocalDateTime settledAt) { this.settledAt = settledAt; }

    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }
}