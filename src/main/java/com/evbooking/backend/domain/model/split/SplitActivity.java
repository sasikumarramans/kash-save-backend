package com.evbooking.backend.domain.model.split;

import java.time.LocalDateTime;

public class SplitActivity {
    private Long id;
    private Long userId; // User who performed the action
    private SplitActivityType activityType;
    private String activityData; // JSON data with activity details
    private Long relatedUserId; // User affected by the action (optional)
    private Long groupId; // Related group (optional)
    private Long splitExpenseId; // Related split expense (optional)
    private LocalDateTime createdAt;

    public SplitActivity() {}

    public SplitActivity(Long userId, SplitActivityType activityType, String activityData,
                        Long relatedUserId, Long groupId, Long splitExpenseId) {
        this.userId = userId;
        this.activityType = activityType;
        this.activityData = activityData;
        this.relatedUserId = relatedUserId;
        this.groupId = groupId;
        this.splitExpenseId = splitExpenseId;
        this.createdAt = LocalDateTime.now();
    }

    // Getters and setters
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public Long getUserId() { return userId; }
    public void setUserId(Long userId) { this.userId = userId; }

    public SplitActivityType getActivityType() { return activityType; }
    public void setActivityType(SplitActivityType activityType) { this.activityType = activityType; }

    public String getActivityData() { return activityData; }
    public void setActivityData(String activityData) { this.activityData = activityData; }

    public Long getRelatedUserId() { return relatedUserId; }
    public void setRelatedUserId(Long relatedUserId) { this.relatedUserId = relatedUserId; }

    public Long getGroupId() { return groupId; }
    public void setGroupId(Long groupId) { this.groupId = groupId; }

    public Long getSplitExpenseId() { return splitExpenseId; }
    public void setSplitExpenseId(Long splitExpenseId) { this.splitExpenseId = splitExpenseId; }

    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }
}