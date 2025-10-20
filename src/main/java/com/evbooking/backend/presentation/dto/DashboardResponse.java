package com.evbooking.backend.presentation.dto;

import java.math.BigDecimal;

public class DashboardResponse {
    private BigDecimal totalExpense;
    private BigDecimal totalIncome;
    private BigDecimal totalSavings;
    private BigDecimal thisMonthExpense;
    private BigDecimal thisMonthIncome;
    private BigDecimal thisMonthSavings;
    private BigDecimal goalAmount;
    private BigDecimal goalProgress;
    private BigDecimal goalReachedAmount;

    public DashboardResponse() {}

    public DashboardResponse(BigDecimal totalExpense, BigDecimal totalIncome,
                           BigDecimal totalSavings, BigDecimal thisMonthExpense,
                           BigDecimal thisMonthIncome, BigDecimal thisMonthSavings,
                           BigDecimal goalAmount, BigDecimal goalProgress,
                           BigDecimal goalReachedAmount) {
        this.totalExpense = totalExpense;
        this.totalIncome = totalIncome;
        this.totalSavings = totalSavings;
        this.thisMonthExpense = thisMonthExpense;
        this.thisMonthIncome = thisMonthIncome;
        this.thisMonthSavings = thisMonthSavings;
        this.goalAmount = goalAmount;
        this.goalProgress = goalProgress;
        this.goalReachedAmount = goalReachedAmount;
    }

    public BigDecimal getTotalExpense() { return totalExpense; }
    public void setTotalExpense(BigDecimal totalExpense) { this.totalExpense = totalExpense; }

    public BigDecimal getTotalIncome() { return totalIncome; }
    public void setTotalIncome(BigDecimal totalIncome) { this.totalIncome = totalIncome; }

    public BigDecimal getTotalSavings() { return totalSavings; }
    public void setTotalSavings(BigDecimal totalSavings) { this.totalSavings = totalSavings; }

    public BigDecimal getThisMonthExpense() { return thisMonthExpense; }
    public void setThisMonthExpense(BigDecimal thisMonthExpense) { this.thisMonthExpense = thisMonthExpense; }

    public BigDecimal getThisMonthIncome() { return thisMonthIncome; }
    public void setThisMonthIncome(BigDecimal thisMonthIncome) { this.thisMonthIncome = thisMonthIncome; }

    public BigDecimal getThisMonthSavings() { return thisMonthSavings; }
    public void setThisMonthSavings(BigDecimal thisMonthSavings) { this.thisMonthSavings = thisMonthSavings; }

    public BigDecimal getGoalAmount() { return goalAmount; }
    public void setGoalAmount(BigDecimal goalAmount) { this.goalAmount = goalAmount; }

    public BigDecimal getGoalProgress() { return goalProgress; }
    public void setGoalProgress(BigDecimal goalProgress) { this.goalProgress = goalProgress; }

    public BigDecimal getGoalReachedAmount() { return goalReachedAmount; }
    public void setGoalReachedAmount(BigDecimal goalReachedAmount) { this.goalReachedAmount = goalReachedAmount; }
}