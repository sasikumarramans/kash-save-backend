package com.evbooking.backend.presentation.dto.split;

import com.evbooking.backend.domain.model.split.SplitActivityType;
import com.fasterxml.jackson.annotation.JsonFormat;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public class SplitActivityResponse {
    private Long id;
    private SplitActivityType activityType;
    private String message; // Human-readable description
    private ActivityActor actor; // Who performed the action
    private ActivityTarget target; // Who was affected (optional)
    private ActivityContext context; // Related group/expense/amount info

    @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss")
    private LocalDateTime timestamp;

    public SplitActivityResponse() {}

    public SplitActivityResponse(Long id, SplitActivityType activityType, String message,
                               ActivityActor actor, ActivityTarget target, ActivityContext context,
                               LocalDateTime timestamp) {
        this.id = id;
        this.activityType = activityType;
        this.message = message;
        this.actor = actor;
        this.target = target;
        this.context = context;
        this.timestamp = timestamp;
    }

    // Getters and setters
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public SplitActivityType getActivityType() { return activityType; }
    public void setActivityType(SplitActivityType activityType) { this.activityType = activityType; }

    public String getMessage() { return message; }
    public void setMessage(String message) { this.message = message; }

    public ActivityActor getActor() { return actor; }
    public void setActor(ActivityActor actor) { this.actor = actor; }

    public ActivityTarget getTarget() { return target; }
    public void setTarget(ActivityTarget target) { this.target = target; }

    public ActivityContext getContext() { return context; }
    public void setContext(ActivityContext context) { this.context = context; }

    public LocalDateTime getTimestamp() { return timestamp; }
    public void setTimestamp(LocalDateTime timestamp) { this.timestamp = timestamp; }

    // Nested classes for structured activity data
    public static class ActivityActor {
        private Long userId;
        private String username;
        private String name;

        public ActivityActor() {}

        public ActivityActor(Long userId, String username, String name) {
            this.userId = userId;
            this.username = username;
            this.name = name;
        }

        public Long getUserId() { return userId; }
        public void setUserId(Long userId) { this.userId = userId; }

        public String getUsername() { return username; }
        public void setUsername(String username) { this.username = username; }

        public String getName() { return name; }
        public void setName(String name) { this.name = name; }
    }

    public static class ActivityTarget {
        private Long userId;
        private String username;
        private String name;

        public ActivityTarget() {}

        public ActivityTarget(Long userId, String username, String name) {
            this.userId = userId;
            this.username = username;
            this.name = name;
        }

        public Long getUserId() { return userId; }
        public void setUserId(Long userId) { this.userId = userId; }

        public String getUsername() { return username; }
        public void setUsername(String username) { this.username = username; }

        public String getName() { return name; }
        public void setName(String name) { this.name = name; }
    }

    public static class ActivityContext {
        private Long groupId;
        private String groupName;
        private Long expenseId;
        private String expenseDescription;
        private BigDecimal amount;
        private String currency;

        public ActivityContext() {}

        public Long getGroupId() { return groupId; }
        public void setGroupId(Long groupId) { this.groupId = groupId; }

        public String getGroupName() { return groupName; }
        public void setGroupName(String groupName) { this.groupName = groupName; }

        public Long getExpenseId() { return expenseId; }
        public void setExpenseId(Long expenseId) { this.expenseId = expenseId; }

        public String getExpenseDescription() { return expenseDescription; }
        public void setExpenseDescription(String expenseDescription) { this.expenseDescription = expenseDescription; }

        public BigDecimal getAmount() { return amount; }
        public void setAmount(BigDecimal amount) { this.amount = amount; }

        public String getCurrency() { return currency; }
        public void setCurrency(String currency) { this.currency = currency; }
    }
}