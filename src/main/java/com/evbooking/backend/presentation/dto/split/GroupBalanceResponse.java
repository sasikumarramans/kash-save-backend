package com.evbooking.backend.presentation.dto.split;

import com.fasterxml.jackson.annotation.JsonFormat;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public class GroupBalanceResponse {
    private Long groupId;
    private String groupName;
    private String groupDescription;
    private String currency;
    private int memberCount;
    private BigDecimal balance; // Positive = group members owe you, Negative = you owe group members
    private BigDecimal youOwe;
    private BigDecimal owesYou;
    private int expenseCount;
    private int settledCount;
    private int pendingCount;
    private boolean isAdmin;

    @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss")
    private LocalDateTime lastActivity;

    public GroupBalanceResponse() {}

    public GroupBalanceResponse(Long groupId, String groupName, String groupDescription,
                              String currency, int memberCount, BigDecimal balance,
                              BigDecimal youOwe, BigDecimal owesYou, int expenseCount,
                              int settledCount, int pendingCount, boolean isAdmin,
                              LocalDateTime lastActivity) {
        this.groupId = groupId;
        this.groupName = groupName;
        this.groupDescription = groupDescription;
        this.currency = currency;
        this.memberCount = memberCount;
        this.balance = balance;
        this.youOwe = youOwe;
        this.owesYou = owesYou;
        this.expenseCount = expenseCount;
        this.settledCount = settledCount;
        this.pendingCount = pendingCount;
        this.isAdmin = isAdmin;
        this.lastActivity = lastActivity;
    }

    // Getters and setters
    public Long getGroupId() { return groupId; }
    public void setGroupId(Long groupId) { this.groupId = groupId; }

    public String getGroupName() { return groupName; }
    public void setGroupName(String groupName) { this.groupName = groupName; }

    public String getGroupDescription() { return groupDescription; }
    public void setGroupDescription(String groupDescription) { this.groupDescription = groupDescription; }

    public String getCurrency() { return currency; }
    public void setCurrency(String currency) { this.currency = currency; }

    public int getMemberCount() { return memberCount; }
    public void setMemberCount(int memberCount) { this.memberCount = memberCount; }

    public BigDecimal getBalance() { return balance; }
    public void setBalance(BigDecimal balance) { this.balance = balance; }

    public BigDecimal getYouOwe() { return youOwe; }
    public void setYouOwe(BigDecimal youOwe) { this.youOwe = youOwe; }

    public BigDecimal getOwesYou() { return owesYou; }
    public void setOwesYou(BigDecimal owesYou) { this.owesYou = owesYou; }

    public int getExpenseCount() { return expenseCount; }
    public void setExpenseCount(int expenseCount) { this.expenseCount = expenseCount; }

    public int getSettledCount() { return settledCount; }
    public void setSettledCount(int settledCount) { this.settledCount = settledCount; }

    public int getPendingCount() { return pendingCount; }
    public void setPendingCount(int pendingCount) { this.pendingCount = pendingCount; }

    public boolean isAdmin() { return isAdmin; }
    public void setAdmin(boolean admin) { isAdmin = admin; }

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