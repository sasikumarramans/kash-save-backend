package com.evbooking.backend.presentation.dto;

import java.math.BigDecimal;

public class CategorySummary {
    private String categoryName;
    private BigDecimal totalAmount;
    private Double percentage;
    private Long transactionCount;

    public CategorySummary() {}

    public CategorySummary(String categoryName, BigDecimal totalAmount, Double percentage, Long transactionCount) {
        this.categoryName = categoryName;
        this.totalAmount = totalAmount;
        this.percentage = percentage;
        this.transactionCount = transactionCount;
    }

    public String getCategoryName() { return categoryName; }
    public void setCategoryName(String categoryName) { this.categoryName = categoryName; }

    public BigDecimal getTotalAmount() { return totalAmount; }
    public void setTotalAmount(BigDecimal totalAmount) { this.totalAmount = totalAmount; }

    public Double getPercentage() { return percentage; }
    public void setPercentage(Double percentage) { this.percentage = percentage; }

    public Long getTransactionCount() { return transactionCount; }
    public void setTransactionCount(Long transactionCount) { this.transactionCount = transactionCount; }
}