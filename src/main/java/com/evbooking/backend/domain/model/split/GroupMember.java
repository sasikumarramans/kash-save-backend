package com.evbooking.backend.domain.model.split;

import java.time.LocalDateTime;

public class GroupMember {
    private Long id;
    private Long groupId;
    private Long userId;
    private boolean isAdmin;
    private LocalDateTime joinedAt;

    public GroupMember() {}

    public GroupMember(Long groupId, Long userId, boolean isAdmin) {
        this.groupId = groupId;
        this.userId = userId;
        this.isAdmin = isAdmin;
        this.joinedAt = LocalDateTime.now();
    }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public Long getGroupId() { return groupId; }
    public void setGroupId(Long groupId) { this.groupId = groupId; }

    public Long getUserId() { return userId; }
    public void setUserId(Long userId) { this.userId = userId; }

    public boolean isAdmin() { return isAdmin; }
    public void setAdmin(boolean admin) { isAdmin = admin; }

    public LocalDateTime getJoinedAt() { return joinedAt; }
    public void setJoinedAt(LocalDateTime joinedAt) { this.joinedAt = joinedAt; }
}