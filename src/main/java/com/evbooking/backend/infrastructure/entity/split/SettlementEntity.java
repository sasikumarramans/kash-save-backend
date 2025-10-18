package com.evbooking.backend.infrastructure.entity.split;

import jakarta.persistence.*;
import org.hibernate.annotations.CreationTimestamp;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "settlements")
public class SettlementEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "from_user_id", nullable = false)
    private String fromUserId;

    @Column(name = "to_user_id", nullable = false)
    private String toUserId;

    @Column(name = "amount", nullable = false, precision = 12, scale = 2)
    private BigDecimal amount;

    @Column(name = "currency", nullable = false, length = 3)
    private String currency = "INR";

    @Column(name = "split_expense_id")
    private Long splitExpenseId;

    @Column(name = "group_id")
    private Long groupId;

    @Column(name = "notes", columnDefinition = "TEXT")
    private String notes;

    @CreationTimestamp
    @Column(name = "settlement_date", nullable = false, updatable = false)
    private LocalDateTime settlementDate;

    @Column(name = "recorded_by_user_id", nullable = false)
    private String recordedByUserId;

    public SettlementEntity() {}

    public SettlementEntity(String fromUserId, String toUserId, BigDecimal amount, String currency,
                           Long splitExpenseId, Long groupId, String notes, String recordedByUserId) {
        this.fromUserId = fromUserId;
        this.toUserId = toUserId;
        this.amount = amount;
        this.currency = currency != null ? currency : "INR";
        this.splitExpenseId = splitExpenseId;
        this.groupId = groupId;
        this.notes = notes;
        this.recordedByUserId = recordedByUserId;
    }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getFromUserId() { return fromUserId; }
    public void setFromUserId(String fromUserId) { this.fromUserId = fromUserId; }

    public String getToUserId() { return toUserId; }
    public void setToUserId(String toUserId) { this.toUserId = toUserId; }

    public BigDecimal getAmount() { return amount; }
    public void setAmount(BigDecimal amount) { this.amount = amount; }

    public String getCurrency() { return currency; }
    public void setCurrency(String currency) { this.currency = currency; }

    public Long getSplitExpenseId() { return splitExpenseId; }
    public void setSplitExpenseId(Long splitExpenseId) { this.splitExpenseId = splitExpenseId; }

    public Long getGroupId() { return groupId; }
    public void setGroupId(Long groupId) { this.groupId = groupId; }

    public String getNotes() { return notes; }
    public void setNotes(String notes) { this.notes = notes; }

    public LocalDateTime getSettlementDate() { return settlementDate; }
    public void setSettlementDate(LocalDateTime settlementDate) { this.settlementDate = settlementDate; }

    public String getRecordedByUserId() { return recordedByUserId; }
    public void setRecordedByUserId(String recordedByUserId) { this.recordedByUserId = recordedByUserId; }
}