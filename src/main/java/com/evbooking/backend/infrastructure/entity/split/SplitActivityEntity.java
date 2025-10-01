package com.evbooking.backend.infrastructure.entity.split;

import com.evbooking.backend.domain.model.split.SplitActivityType;
import jakarta.persistence.*;

import java.time.LocalDateTime;

@Entity
@Table(name = "split_activities")
public class SplitActivityEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "user_id", nullable = false)
    private Long userId;

    @Enumerated(EnumType.STRING)
    @Column(name = "activity_type", nullable = false)
    private SplitActivityType activityType;

    @Column(name = "activity_data", columnDefinition = "TEXT")
    private String activityData;

    @Column(name = "related_user_id")
    private Long relatedUserId;

    @Column(name = "group_id")
    private Long groupId;

    @Column(name = "split_expense_id")
    private Long splitExpenseId;

    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt;

    @PrePersist
    public void prePersist() {
        if (createdAt == null) {
            createdAt = LocalDateTime.now();
        }
    }

    public SplitActivityEntity() {}

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