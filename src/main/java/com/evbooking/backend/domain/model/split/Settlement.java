package com.evbooking.backend.domain.model.split;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public class Settlement {
    private Long id;
    private String fromUserId;
    private String toUserId;
    private BigDecimal amount;
    private String currency;
    private Long splitExpenseId;
    private String notes;
    private LocalDateTime settlementDate;
    private String recordedByUserId;

    public Settlement() {}

    public Settlement(String fromUserId, String toUserId, BigDecimal amount, String currency,
                     Long splitExpenseId, String notes, String recordedByUserId) {
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

    public String getNotes() { return notes; }
    public void setNotes(String notes) { this.notes = notes; }

    public LocalDateTime getSettlementDate() { return settlementDate; }
    public void setSettlementDate(LocalDateTime settlementDate) { this.settlementDate = settlementDate; }

    public String getRecordedByUserId() { return recordedByUserId; }
    public void setRecordedByUserId(String recordedByUserId) { this.recordedByUserId = recordedByUserId; }
}