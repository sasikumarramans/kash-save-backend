package com.evbooking.backend.infrastructure.entity.split;

import jakarta.persistence.*;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDateTime;

@Entity
@Table(name = "group_deletion_history", indexes = {
    @Index(name = "idx_group_deletion_group_id", columnList = "group_id"),
    @Index(name = "idx_group_deletion_deleted_by", columnList = "deleted_by_user_id"),
    @Index(name = "idx_group_deletion_deleted_at", columnList = "deleted_at")
})
public class GroupDeletionHistoryEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "group_id", nullable = false)
    private Long groupId;

    @Column(name = "group_name", length = 255, nullable = false)
    private String groupName;

    @Column(name = "group_description", length = 1000)
    private String groupDescription;

    @Column(name = "deleted_by_user_id", nullable = false)
    private String deletedByUserId;

    @Column(name = "deleted_by_username", length = 50)
    private String deletedByUsername;

    @CreationTimestamp
    @Column(name = "deleted_at", nullable = false, updatable = false)
    private LocalDateTime deletedAt;

    @Column(name = "member_count", nullable = false)
    private int memberCount;

    @Column(name = "reason", length = 500)
    private String reason;

    public GroupDeletionHistoryEntity() {}

    // Getters and Setters
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public Long getGroupId() { return groupId; }
    public void setGroupId(Long groupId) { this.groupId = groupId; }

    public String getGroupName() { return groupName; }
    public void setGroupName(String groupName) { this.groupName = groupName; }

    public String getGroupDescription() { return groupDescription; }
    public void setGroupDescription(String groupDescription) { this.groupDescription = groupDescription; }

    public String getDeletedByUserId() { return deletedByUserId; }
    public void setDeletedByUserId(String deletedByUserId) { this.deletedByUserId = deletedByUserId; }

    public String getDeletedByUsername() { return deletedByUsername; }
    public void setDeletedByUsername(String deletedByUsername) { this.deletedByUsername = deletedByUsername; }

    public LocalDateTime getDeletedAt() { return deletedAt; }
    public void setDeletedAt(LocalDateTime deletedAt) { this.deletedAt = deletedAt; }

    public int getMemberCount() { return memberCount; }
    public void setMemberCount(int memberCount) { this.memberCount = memberCount; }

    public String getReason() { return reason; }
    public void setReason(String reason) { this.reason = reason; }
}
