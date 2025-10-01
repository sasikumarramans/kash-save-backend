package com.evbooking.backend.presentation.dto.split;

import com.evbooking.backend.domain.model.split.SplitType;
import jakarta.validation.Valid;
import jakarta.validation.constraints.*;

import java.math.BigDecimal;
import java.util.List;

public class CreateSplitExpenseRequest {
    @NotBlank(message = "Description is required")
    @Size(max = 500, message = "Description cannot exceed 500 characters")
    private String description;

    @NotNull(message = "Total amount is required")
    @DecimalMin(value = "0.01", message = "Amount must be greater than 0")
    private BigDecimal totalAmount;

    private String currency = "INR";

    @NotBlank(message = "Paid by username is required")
    private String paidByUsername;

    private Long groupId;

    @NotNull(message = "Split type is required")
    private SplitType splitType;

    @Valid
    @NotEmpty(message = "At least one participant is required")
    private List<SplitParticipantRequest> participants;

    public CreateSplitExpenseRequest() {}

    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }

    public BigDecimal getTotalAmount() { return totalAmount; }
    public void setTotalAmount(BigDecimal totalAmount) { this.totalAmount = totalAmount; }

    public String getCurrency() { return currency; }
    public void setCurrency(String currency) { this.currency = currency; }

    public String getPaidByUsername() { return paidByUsername; }
    public void setPaidByUsername(String paidByUsername) { this.paidByUsername = paidByUsername; }

    public Long getGroupId() { return groupId; }
    public void setGroupId(Long groupId) { this.groupId = groupId; }

    public SplitType getSplitType() { return splitType; }
    public void setSplitType(SplitType splitType) { this.splitType = splitType; }

    public List<SplitParticipantRequest> getParticipants() { return participants; }
    public void setParticipants(List<SplitParticipantRequest> participants) { this.participants = participants; }

    public static class SplitParticipantRequest {
        @NotBlank(message = "Participant username is required")
        private String username;

        private BigDecimal splitValue;

        public SplitParticipantRequest() {}

        public SplitParticipantRequest(String username, BigDecimal splitValue) {
            this.username = username;
            this.splitValue = splitValue;
        }

        public String getUsername() { return username; }
        public void setUsername(String username) { this.username = username; }

        public BigDecimal getSplitValue() { return splitValue; }
        public void setSplitValue(BigDecimal splitValue) { this.splitValue = splitValue; }
    }
}