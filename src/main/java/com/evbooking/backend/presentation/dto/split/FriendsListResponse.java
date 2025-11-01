package com.evbooking.backend.presentation.dto.split;

import com.fasterxml.jackson.annotation.JsonFormat;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

/**
 * Response DTO for friends list with their latest expenses
 */
public class FriendsListResponse {
    private String userId;
    private String username;
    private String name;
    private String email;
    private BigDecimal overallPayingAmount;   // Total amount you owe to this friend
    private BigDecimal overallReceivingAmount; // Total amount you should receive from this friend
    private List<RecentExpenseDetail> recentExpenses;

    public FriendsListResponse() {}

    public FriendsListResponse(String userId, String username, String name, String email,
                              BigDecimal overallPayingAmount, BigDecimal overallReceivingAmount,
                              List<RecentExpenseDetail> recentExpenses) {
        this.userId = userId;
        this.username = username;
        this.name = name;
        this.email = email;
        this.overallPayingAmount = overallPayingAmount;
        this.overallReceivingAmount = overallReceivingAmount;
        this.recentExpenses = recentExpenses;
    }

    // Getters and setters
    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public BigDecimal getOverallPayingAmount() {
        return overallPayingAmount;
    }

    public void setOverallPayingAmount(BigDecimal overallPayingAmount) {
        this.overallPayingAmount = overallPayingAmount;
    }

    public BigDecimal getOverallReceivingAmount() {
        return overallReceivingAmount;
    }

    public void setOverallReceivingAmount(BigDecimal overallReceivingAmount) {
        this.overallReceivingAmount = overallReceivingAmount;
    }

    public List<RecentExpenseDetail> getRecentExpenses() {
        return recentExpenses;
    }

    public void setRecentExpenses(List<RecentExpenseDetail> recentExpenses) {
        this.recentExpenses = recentExpenses;
    }

    /**
     * Recent expense detail for a friend
     */
    public static class RecentExpenseDetail {
        private Long expenseId;
        private Long groupId;
        private String groupName;
        private String description;
        private BigDecimal totalAmount;
        private String currency;
        private BigDecimal yourAmount; // Positive if you pay, negative if you receive
        private String status; // "You pay" or "You receive"

        @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss")
        private LocalDateTime createdAt;

        public RecentExpenseDetail() {}

        public RecentExpenseDetail(Long expenseId, Long groupId, String groupName, String description,
                                  BigDecimal totalAmount, String currency, BigDecimal yourAmount,
                                  String status, LocalDateTime createdAt) {
            this.expenseId = expenseId;
            this.groupId = groupId;
            this.groupName = groupName;
            this.description = description;
            this.totalAmount = totalAmount;
            this.currency = currency;
            this.yourAmount = yourAmount;
            this.status = status;
            this.createdAt = createdAt;
        }

        public Long getExpenseId() {
            return expenseId;
        }

        public void setExpenseId(Long expenseId) {
            this.expenseId = expenseId;
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

        public BigDecimal getYourAmount() {
            return yourAmount;
        }

        public void setYourAmount(BigDecimal yourAmount) {
            this.yourAmount = yourAmount;
        }

        public String getStatus() {
            return status;
        }

        public void setStatus(String status) {
            this.status = status;
        }

        public LocalDateTime getCreatedAt() {
            return createdAt;
        }

        public void setCreatedAt(LocalDateTime createdAt) {
            this.createdAt = createdAt;
        }
    }
}