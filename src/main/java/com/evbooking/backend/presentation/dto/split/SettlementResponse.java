package com.evbooking.backend.presentation.dto.split;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public class SettlementResponse {

    private Long id;
    private String fromUserId;
    private String fromUsername;
    private String toUserId;
    private String toUsername;
    private BigDecimal amount;
    private String currency;
    private Long splitExpenseId;
    private Long groupId;
    private String groupName;
    private String notes;
    private LocalDateTime settlementDate;
    private String recordedByUserId;
    private String recordedByUsername;

    public SettlementResponse() {}

    public SettlementResponse(Long id, String fromUserId, String fromUsername,
                            String toUserId, String toUsername, BigDecimal amount,
                            String currency, Long splitExpenseId, Long groupId,
                            String groupName, String notes, LocalDateTime settlementDate,
                            String recordedByUserId, String recordedByUsername) {
        this.id = id;
        this.fromUserId = fromUserId;
        this.fromUsername = fromUsername;
        this.toUserId = toUserId;
        this.toUsername = toUsername;
        this.amount = amount;
        this.currency = currency;
        this.splitExpenseId = splitExpenseId;
        this.groupId = groupId;
        this.groupName = groupName;
        this.notes = notes;
        this.settlementDate = settlementDate;
        this.recordedByUserId = recordedByUserId;
        this.recordedByUsername = recordedByUsername;
    }

    // Helper methods
    public boolean isStandaloneSettlement() {
        return splitExpenseId == null;
    }

    public boolean isGroupSettlement() {
        return groupId != null;
    }

    // Getters and Setters
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getFromUserId() { return fromUserId; }
    public void setFromUserId(String fromUserId) { this.fromUserId = fromUserId; }

    public String getFromUsername() { return fromUsername; }
    public void setFromUsername(String fromUsername) { this.fromUsername = fromUsername; }

    public String getToUserId() { return toUserId; }
    public void setToUserId(String toUserId) { this.toUserId = toUserId; }

    public String getToUsername() { return toUsername; }
    public void setToUsername(String toUsername) { this.toUsername = toUsername; }

    public BigDecimal getAmount() { return amount; }
    public void setAmount(BigDecimal amount) { this.amount = amount; }

    public String getCurrency() { return currency; }
    public void setCurrency(String currency) { this.currency = currency; }

    public Long getSplitExpenseId() { return splitExpenseId; }
    public void setSplitExpenseId(Long splitExpenseId) { this.splitExpenseId = splitExpenseId; }

    public Long getGroupId() { return groupId; }
    public void setGroupId(Long groupId) { this.groupId = groupId; }

    public String getGroupName() { return groupName; }
    public void setGroupName(String groupName) { this.groupName = groupName; }

    public String getNotes() { return notes; }
    public void setNotes(String notes) { this.notes = notes; }

    public LocalDateTime getSettlementDate() { return settlementDate; }
    public void setSettlementDate(LocalDateTime settlementDate) { this.settlementDate = settlementDate; }

    public String getRecordedByUserId() { return recordedByUserId; }
    public void setRecordedByUserId(String recordedByUserId) { this.recordedByUserId = recordedByUserId; }

    public String getRecordedByUsername() { return recordedByUsername; }
    public void setRecordedByUsername(String recordedByUsername) { this.recordedByUsername = recordedByUsername; }
}
