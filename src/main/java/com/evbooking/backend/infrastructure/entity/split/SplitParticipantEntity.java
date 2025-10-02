package com.evbooking.backend.infrastructure.entity.split;

import jakarta.persistence.*;
import org.hibernate.annotations.CreationTimestamp;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "split_participants",
       uniqueConstraints = @UniqueConstraint(columnNames = {"split_expense_id", "user_id"}))
public class SplitParticipantEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "split_expense_id", nullable = false)
    private Long splitExpenseId;

    @Column(name = "user_id", nullable = false)
    private Long userId;

    @Column(name = "amount_owed", nullable = false, precision = 12, scale = 2)
    private BigDecimal amountOwed;

    @Column(name = "split_value", precision = 12, scale = 4)
    private BigDecimal splitValue;

    @Column(name = "is_settled", nullable = false)
    private boolean isSettled = false;

    @Column(name = "settled_at")
    private LocalDateTime settledAt;

    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    public SplitParticipantEntity() {}

    public SplitParticipantEntity(Long splitExpenseId, Long userId, BigDecimal amountOwed, BigDecimal splitValue) {
        this.splitExpenseId = splitExpenseId;
        this.userId = userId;
        this.amountOwed = amountOwed;
        this.splitValue = splitValue;
        this.isSettled = false;
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
    public void setSettled(boolean settled) { isSettled = settled; }

    public LocalDateTime getSettledAt() { return settledAt; }
    public void setSettledAt(LocalDateTime settledAt) { this.settledAt = settledAt; }

    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }
}