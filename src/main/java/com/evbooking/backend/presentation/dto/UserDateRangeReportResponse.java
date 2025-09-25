package com.evbooking.backend.presentation.dto;

import com.fasterxml.jackson.annotation.JsonFormat;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public class UserDateRangeReportResponse {
    private BigDecimal totalExpense;
    private BigDecimal totalIncome;
    private BigDecimal balance;

    @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss")
    private LocalDateTime startDate;

    @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss")
    private LocalDateTime endDate;

    public UserDateRangeReportResponse() {}

    public UserDateRangeReportResponse(BigDecimal totalExpense, BigDecimal totalIncome, BigDecimal balance,
                                      LocalDateTime startDate, LocalDateTime endDate) {
        this.totalExpense = totalExpense;
        this.totalIncome = totalIncome;
        this.balance = balance;
        this.startDate = startDate;
        this.endDate = endDate;
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
}