package com.evbooking.backend.presentation.dto.split;

import com.evbooking.backend.domain.model.split.SplitType;
import com.fasterxml.jackson.annotation.JsonFormat;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

public class SplitExpenseResponse {
    private Long id;
    private String description;
    private BigDecimal totalAmount;
    private String currency;
    private String paidByUsername;
    private Long paidByUserId;
    private Long groupId;
    private String groupName;
    private SplitType splitType;
    private String createdByUsername;
    private Long createdByUserId;
    private List<SplitParticipantResponse> participants;

    @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss")
    private LocalDateTime createdAt;

    public SplitExpenseResponse() {}

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }

    public BigDecimal getTotalAmount() { return totalAmount; }
    public void setTotalAmount(BigDecimal totalAmount) { this.totalAmount = totalAmount; }

    public String getCurrency() { return currency; }
    public void setCurrency(String currency) { this.currency = currency; }

    public String getPaidByUsername() { return paidByUsername; }
    public void setPaidByUsername(String paidByUsername) { this.paidByUsername = paidByUsername; }

    public Long getPaidByUserId() { return paidByUserId; }
    public void setPaidByUserId(Long paidByUserId) { this.paidByUserId = paidByUserId; }

    public Long getGroupId() { return groupId; }
    public void setGroupId(Long groupId) { this.groupId = groupId; }

    public String getGroupName() { return groupName; }
    public void setGroupName(String groupName) { this.groupName = groupName; }

    public SplitType getSplitType() { return splitType; }
    public void setSplitType(SplitType splitType) { this.splitType = splitType; }

    public String getCreatedByUsername() { return createdByUsername; }
    public void setCreatedByUsername(String createdByUsername) { this.createdByUsername = createdByUsername; }

    public Long getCreatedByUserId() { return createdByUserId; }
    public void setCreatedByUserId(Long createdByUserId) { this.createdByUserId = createdByUserId; }

    public List<SplitParticipantResponse> getParticipants() { return participants; }
    public void setParticipants(List<SplitParticipantResponse> participants) { this.participants = participants; }

    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }

    public static class SplitParticipantResponse {
        private Long userId;
        private String username;
        private BigDecimal amountOwed;
        private BigDecimal splitValue;
        private boolean isSettled;

        @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss")
        private LocalDateTime settledAt;

        public SplitParticipantResponse() {}

        public Long getUserId() { return userId; }
        public void setUserId(Long userId) { this.userId = userId; }

        public String getUsername() { return username; }
        public void setUsername(String username) { this.username = username; }

        public BigDecimal getAmountOwed() { return amountOwed; }
        public void setAmountOwed(BigDecimal amountOwed) { this.amountOwed = amountOwed; }

        public BigDecimal getSplitValue() { return splitValue; }
        public void setSplitValue(BigDecimal splitValue) { this.splitValue = splitValue; }

        public boolean isSettled() { return isSettled; }
        public void setSettled(boolean settled) { isSettled = settled; }

        public LocalDateTime getSettledAt() { return settledAt; }
        public void setSettledAt(LocalDateTime settledAt) { this.settledAt = settledAt; }
    }
}