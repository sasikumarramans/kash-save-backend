package com.evbooking.backend.presentation.dto.split;

import java.math.BigDecimal;

public class OverallBalanceSummaryResponse {
    private BigDecimal totalYouOwe;
    private BigDecimal totalOwesYou;
    private BigDecimal netBalance; // Positive = more owed to you, Negative = you owe more
    private String primaryCurrency;

    // Friends summary
    private int totalFriends;
    private int friendsYouOwe;
    private int friendsWhoOweYou;
    private int settledFriends;

    // Groups summary
    private int totalGroups;
    private int groupsYouOwe;
    private int groupsWhoOweYou;
    private int settledGroups;

    // Activity summary
    private int totalExpenses;
    private int settledExpenses;
    private int pendingExpenses;

    public OverallBalanceSummaryResponse() {}

    public OverallBalanceSummaryResponse(BigDecimal totalYouOwe, BigDecimal totalOwesYou,
                                       BigDecimal netBalance, String primaryCurrency,
                                       int totalFriends, int friendsYouOwe, int friendsWhoOweYou,
                                       int settledFriends, int totalGroups, int groupsYouOwe,
                                       int groupsWhoOweYou, int settledGroups, int totalExpenses,
                                       int settledExpenses, int pendingExpenses) {
        this.totalYouOwe = totalYouOwe;
        this.totalOwesYou = totalOwesYou;
        this.netBalance = netBalance;
        this.primaryCurrency = primaryCurrency;
        this.totalFriends = totalFriends;
        this.friendsYouOwe = friendsYouOwe;
        this.friendsWhoOweYou = friendsWhoOweYou;
        this.settledFriends = settledFriends;
        this.totalGroups = totalGroups;
        this.groupsYouOwe = groupsYouOwe;
        this.groupsWhoOweYou = groupsWhoOweYou;
        this.settledGroups = settledGroups;
        this.totalExpenses = totalExpenses;
        this.settledExpenses = settledExpenses;
        this.pendingExpenses = pendingExpenses;
    }

    // Getters and setters
    public BigDecimal getTotalYouOwe() { return totalYouOwe; }
    public void setTotalYouOwe(BigDecimal totalYouOwe) { this.totalYouOwe = totalYouOwe; }

    public BigDecimal getTotalOwesYou() { return totalOwesYou; }
    public void setTotalOwesYou(BigDecimal totalOwesYou) { this.totalOwesYou = totalOwesYou; }

    public BigDecimal getNetBalance() { return netBalance; }
    public void setNetBalance(BigDecimal netBalance) { this.netBalance = netBalance; }

    public String getPrimaryCurrency() { return primaryCurrency; }
    public void setPrimaryCurrency(String primaryCurrency) { this.primaryCurrency = primaryCurrency; }

    public int getTotalFriends() { return totalFriends; }
    public void setTotalFriends(int totalFriends) { this.totalFriends = totalFriends; }

    public int getFriendsYouOwe() { return friendsYouOwe; }
    public void setFriendsYouOwe(int friendsYouOwe) { this.friendsYouOwe = friendsYouOwe; }

    public int getFriendsWhoOweYou() { return friendsWhoOweYou; }
    public void setFriendsWhoOweYou(int friendsWhoOweYou) { this.friendsWhoOweYou = friendsWhoOweYou; }

    public int getSettledFriends() { return settledFriends; }
    public void setSettledFriends(int settledFriends) { this.settledFriends = settledFriends; }

    public int getTotalGroups() { return totalGroups; }
    public void setTotalGroups(int totalGroups) { this.totalGroups = totalGroups; }

    public int getGroupsYouOwe() { return groupsYouOwe; }
    public void setGroupsYouOwe(int groupsYouOwe) { this.groupsYouOwe = groupsYouOwe; }

    public int getGroupsWhoOweYou() { return groupsWhoOweYou; }
    public void setGroupsWhoOweYou(int groupsWhoOweYou) { this.groupsWhoOweYou = groupsWhoOweYou; }

    public int getSettledGroups() { return settledGroups; }
    public void setSettledGroups(int settledGroups) { this.settledGroups = settledGroups; }

    public int getTotalExpenses() { return totalExpenses; }
    public void setTotalExpenses(int totalExpenses) { this.totalExpenses = totalExpenses; }

    public int getSettledExpenses() { return settledExpenses; }
    public void setSettledExpenses(int settledExpenses) { this.settledExpenses = settledExpenses; }

    public int getPendingExpenses() { return pendingExpenses; }
    public void setPendingExpenses(int pendingExpenses) { this.pendingExpenses = pendingExpenses; }
}