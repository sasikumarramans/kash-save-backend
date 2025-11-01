package com.evbooking.backend.presentation.dto.split;

import com.fasterxml.jackson.annotation.JsonFormat;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * Simplified response DTO for expense lists.
 * Contains only essential information and current user's share.
 */
public class SplitExpenseSummaryResponse {
    private Long expenseId;
    private String description;
    private BigDecimal totalAmount;
    private String currency;
    private String paidByUsername;
    private String paidByUserId;

    // Current user's amount: positive if they owe, negative if they should receive
    private BigDecimal currentUserAmount;

    private Long groupId;
    private String groupName;

    @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss")
    private LocalDateTime createdAt;

    public SplitExpenseSummaryResponse() {}

    public SplitExpenseSummaryResponse(Long expenseId, String description, BigDecimal totalAmount,
                                      String currency, String paidByUsername, String paidByUserId,
                                      BigDecimal currentUserAmount, Long groupId, String groupName,
                                      LocalDateTime createdAt) {
        this.expenseId = expenseId;
        this.description = description;
        this.totalAmount = totalAmount;
        this.currency = currency;
        this.paidByUsername = paidByUsername;
        this.paidByUserId = paidByUserId;
        this.currentUserAmount = currentUserAmount;
        this.groupId = groupId;
        this.groupName = groupName;
        this.createdAt = createdAt;
    }

    // Getters and setters
    public Long getExpenseId() {
        return expenseId;
    }

    public void setExpenseId(Long expenseId) {
        this.expenseId = expenseId;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public BigDecimal getTotalAmount() {
        return totalAmount;
    }

    public void setTotalAmount(BigDecimal totalAmount) {
        this.totalAmount = totalAmount;
    }

    public String getCurrency() {
        return currency;
    }

    public void setCurrency(String currency) {
        this.currency = currency;
    }

    public String getPaidByUsername() {
        return paidByUsername;
    }

    public void setPaidByUsername(String paidByUsername) {
        this.paidByUsername = paidByUsername;
    }

    public String getPaidByUserId() {
        return paidByUserId;
    }

    public void setPaidByUserId(String paidByUserId) {
        this.paidByUserId = paidByUserId;
    }

    public BigDecimal getCurrentUserAmount() {
        return currentUserAmount;
    }

    public void setCurrentUserAmount(BigDecimal currentUserAmount) {
        this.currentUserAmount = currentUserAmount;
    }

    public Long getGroupId() {
        return groupId;
    }

    public void setGroupId(Long groupId) {
        this.groupId = groupId;
    }

    public String getGroupName() {
        return groupName;
    }

    public void setGroupName(String groupName) {
        this.groupName = groupName;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }
}