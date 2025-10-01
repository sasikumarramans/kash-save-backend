package com.evbooking.backend.presentation.dto.split;

import com.fasterxml.jackson.annotation.JsonFormat;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public class FriendBalanceResponse {
    private Long friendUserId;
    private String friendUsername;
    private String friendEmail;
    private BigDecimal balance; // Positive = they owe you, Negative = you owe them
    private BigDecimal youOwe;
    private BigDecimal owesYou;
    private String currency;
    private int expenseCount;
    private int settledCount;
    private int pendingCount;

    @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss")
    private LocalDateTime lastActivity;

    public FriendBalanceResponse() {}

    public FriendBalanceResponse(Long friendUserId, String friendUsername, String friendEmail,
                               BigDecimal balance, BigDecimal youOwe, BigDecimal owesYou,
                               String currency, int expenseCount, int settledCount, int pendingCount,
                               LocalDateTime lastActivity) {
        this.friendUserId = friendUserId;
        this.friendUsername = friendUsername;
        this.friendEmail = friendEmail;
        this.balance = balance;
        this.youOwe = youOwe;
        this.owesYou = owesYou;
        this.currency = currency;
        this.expenseCount = expenseCount;
        this.settledCount = settledCount;
        this.pendingCount = pendingCount;
        this.lastActivity = lastActivity;
    }

    // Getters and setters
    public Long getFriendUserId() { return friendUserId; }
    public void setFriendUserId(Long friendUserId) { this.friendUserId = friendUserId; }

    public String getFriendUsername() { return friendUsername; }
    public void setFriendUsername(String friendUsername) { this.friendUsername = friendUsername; }

    public String getFriendEmail() { return friendEmail; }
    public void setFriendEmail(String friendEmail) { this.friendEmail = friendEmail; }

    public BigDecimal getBalance() { return balance; }
    public void setBalance(BigDecimal balance) { this.balance = balance; }

    public BigDecimal getYouOwe() { return youOwe; }
    public void setYouOwe(BigDecimal youOwe) { this.youOwe = youOwe; }

    public BigDecimal getOwesYou() { return owesYou; }
    public void setOwesYou(BigDecimal owesYou) { this.owesYou = owesYou; }

    public String getCurrency() { return currency; }
    public void setCurrency(String currency) { this.currency = currency; }

    public int getExpenseCount() { return expenseCount; }
    public void setExpenseCount(int expenseCount) { this.expenseCount = expenseCount; }

    public int getSettledCount() { return settledCount; }
    public void setSettledCount(int settledCount) { this.settledCount = settledCount; }

    public int getPendingCount() { return pendingCount; }
    public void setPendingCount(int pendingCount) { this.pendingCount = pendingCount; }

    public LocalDateTime getLastActivity() { return lastActivity; }
    public void setLastActivity(LocalDateTime lastActivity) { this.lastActivity = lastActivity; }

    // Helper methods for filtering
    public boolean hasOutstandingBalance() {
        return balance.compareTo(BigDecimal.ZERO) != 0;
    }

    public boolean youOweAmount() {
        return balance.compareTo(BigDecimal.ZERO) < 0;
    }

    public boolean owesYouAmount() {
        return balance.compareTo(BigDecimal.ZERO) > 0;
    }

    public boolean isSettled() {
        return balance.compareTo(BigDecimal.ZERO) == 0;
    }
}