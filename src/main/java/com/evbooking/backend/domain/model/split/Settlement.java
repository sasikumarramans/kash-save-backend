package com.evbooking.backend.domain.model.split;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public class Settlement {
    private Long id;
    private Long fromUserId;
    private Long toUserId;
    private BigDecimal amount;
    private String currency;
    private Long splitExpenseId;
    private String notes;
    private LocalDateTime settlementDate;
    private Long recordedByUserId;

    public Settlement() {}

    public Settlement(Long fromUserId, Long toUserId, BigDecimal amount, String currency,
                     Long splitExpenseId, String notes, Long recordedByUserId) {
        this.fromUserId = fromUserId;
        this.toUserId = toUserId;
        this.amount = amount;
        this.currency = currency != null ? currency : "INR";
        this.splitExpenseId = splitExpenseId;
        this.notes = notes;
        this.recordedByUserId = recordedByUserId;
        this.settlementDate = LocalDateTime.now();
    }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public Long getFromUserId() { return fromUserId; }
    public void setFromUserId(Long fromUserId) { this.fromUserId = fromUserId; }

    public Long getToUserId() { return toUserId; }
    public void setToUserId(Long toUserId) { this.toUserId = toUserId; }

    public BigDecimal getAmount() { return amount; }
    public void setAmount(BigDecimal amount) { this.amount = amount; }

    public String getCurrency() { return currency; }
    public void setCurrency(String currency) { this.currency = currency; }

    public Long getSplitExpenseId() { return splitExpenseId; }
    public void setSplitExpenseId(Long splitExpenseId) { this.splitExpenseId = splitExpenseId; }

    public String getNotes() { return notes; }
    public void setNotes(String notes) { this.notes = notes; }

    public LocalDateTime getSettlementDate() { return settlementDate; }
    public void setSettlementDate(LocalDateTime settlementDate) { this.settlementDate = settlementDate; }

    public Long getRecordedByUserId() { return recordedByUserId; }
    public void setRecordedByUserId(Long recordedByUserId) { this.recordedByUserId = recordedByUserId; }
}