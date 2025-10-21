package com.evbooking.backend.presentation.dto;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

public class CategoryReportResponse {
    private BigDecimal totalExpense;
    private BigDecimal totalIncome;
    private BigDecimal balance;
    private LocalDateTime startDate;
    private LocalDateTime endDate;
    private String period;
    private List<CategorySummary> expenseCategories;
    private List<CategorySummary> incomeCategories;

    public CategoryReportResponse() {}

    public CategoryReportResponse(BigDecimal totalExpense, BigDecimal totalIncome, BigDecimal balance,
                                 LocalDateTime startDate, LocalDateTime endDate, String period,
                                 List<CategorySummary> expenseCategories, List<CategorySummary> incomeCategories) {
        this.totalExpense = totalExpense;
        this.totalIncome = totalIncome;
        this.balance = balance;
        this.startDate = startDate;
        this.endDate = endDate;
        this.period = period;
        this.expenseCategories = expenseCategories;
        this.incomeCategories = incomeCategories;
    }

    public BigDecimal getTotalExpense() { return totalExpense; }
    public void setTotalExpense(BigDecimal totalExpense) { this.totalExpense = totalExpense; }

    public BigDecimal getTotalIncome() { return totalIncome; }
    public void setTotalIncome(BigDecimal totalIncome) { this.totalIncome = totalIncome; }

    public BigDecimal getBalance() { return balance; }
    public void setBalance(BigDecimal balance) { this.balance = balance; }

    public LocalDateTime getStartDate() { return startDate; }
    public void setStartDate(LocalDateTime startDate) { this.startDate = startDate; }

    public LocalDateTime getEndDate() { return endDate; }
    public void setEndDate(LocalDateTime endDate) { this.endDate = endDate; }

    public String getPeriod() { return period; }
    public void setPeriod(String period) { this.period = period; }

    public List<CategorySummary> getExpenseCategories() { return expenseCategories; }
    public void setExpenseCategories(List<CategorySummary> expenseCategories) { this.expenseCategories = expenseCategories; }

    public List<CategorySummary> getIncomeCategories() { return incomeCategories; }
    public void setIncomeCategories(List<CategorySummary> incomeCategories) { this.incomeCategories = incomeCategories; }
}