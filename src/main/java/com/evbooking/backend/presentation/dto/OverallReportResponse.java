package com.evbooking.backend.presentation.dto;

import java.math.BigDecimal;

public class OverallReportResponse {
    private BigDecimal totalExpense;
    private BigDecimal totalIncome;
    private BigDecimal balance;

    public OverallReportResponse() {}

    public OverallReportResponse(BigDecimal totalExpense, BigDecimal totalIncome, BigDecimal balance) {
        this.totalExpense = totalExpense;
        this.totalIncome = totalIncome;
        this.balance = balance;
    }

    public BigDecimal getTotalExpense() { return totalExpense; }
    public void setTotalExpense(BigDecimal totalExpense) { this.totalExpense = totalExpense; }

    public BigDecimal getTotalIncome() { return totalIncome; }
    public void setTotalIncome(BigDecimal totalIncome) { this.totalIncome = totalIncome; }

    public BigDecimal getBalance() { return balance; }
    public void setBalance(BigDecimal balance) { this.balance = balance; }
}