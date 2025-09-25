package com.evbooking.backend.domain.service;

import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Service
public class ReportDomainService {

    public void validateDateRange(LocalDateTime startDate, LocalDateTime endDate) {
        if (startDate == null || endDate == null) {
            throw new RuntimeException("Start date and end date are required");
        }

        if (startDate.isAfter(endDate)) {
            throw new RuntimeException("Start date cannot be after end date");
        }
    }

    public BigDecimal calculateBalance(BigDecimal totalIncome, BigDecimal totalExpense) {
        if (totalIncome == null) totalIncome = BigDecimal.ZERO;
        if (totalExpense == null) totalExpense = BigDecimal.ZERO;

        return totalIncome.subtract(totalExpense);
    }

    public BigDecimal calculateSavings(BigDecimal totalIncome, BigDecimal totalExpense) {
        return calculateBalance(totalIncome, totalExpense);
    }
}