package com.evbooking.backend.domain.model.split;

import java.time.LocalDateTime;

public class GroupDeletionHistory {
    private Long id;
    private Long groupId;
    private String groupName;
    private String groupDescription;
    private String deletedByUserId;
    private String deletedByUsername;
    private LocalDateTime deletedAt;
    private int memberCount;
    private String reason;

    public GroupDeletionHistory() {}

    public GroupDeletionHistory(Long groupId, String groupName, String groupDescription,
                               String deletedByUserId, String deletedByUsername,
                               int memberCount, String reason) {
        this.groupId = groupId;
        this.groupName = groupName;
        this.groupDescription = groupDescription;
        this.deletedByUserId = deletedByUserId;
        this.deletedByUsername = deletedByUsername;
        this.deletedAt = LocalDateTime.now();
        this.memberCount = memberCount;
        this.reason = reason;
    }

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
