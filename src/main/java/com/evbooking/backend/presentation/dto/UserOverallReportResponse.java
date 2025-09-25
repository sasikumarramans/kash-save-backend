package com.evbooking.backend.presentation.dto;

import java.math.BigDecimal;

public class UserOverallReportResponse {
    private BigDecimal totalExpense;
    private BigDecimal totalIncome;
    private BigDecimal overallSavings;
    private BigDecimal currentMonthSavings;

    public UserOverallReportResponse() {}

    public UserOverallReportResponse(BigDecimal totalExpense, BigDecimal totalIncome,
                                    BigDecimal overallSavings, BigDecimal currentMonthSavings) {
        this.totalExpense = totalExpense;
        this.totalIncome = totalIncome;
        this.overallSavings = overallSavings;
        this.currentMonthSavings = currentMonthSavings;
    }

    public BigDecimal getTotalExpense() { return totalExpense; }
    public void setTotalExpense(BigDecimal totalExpense) { this.totalExpense = totalExpense; }

    public BigDecimal getTotalIncome() { return totalIncome; }
    public void setTotalIncome(BigDecimal totalIncome) { this.totalIncome = totalIncome; }

    public BigDecimal getOverallSavings() { return overallSavings; }
    public void setOverallSavings(BigDecimal overallSavings) { this.overallSavings = overallSavings; }

    public BigDecimal getCurrentMonthSavings() { return currentMonthSavings; }
    public void setCurrentMonthSavings(BigDecimal currentMonthSavings) { this.currentMonthSavings = currentMonthSavings; }
}