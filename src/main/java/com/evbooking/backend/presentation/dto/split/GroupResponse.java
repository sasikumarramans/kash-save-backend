package com.evbooking.backend.presentation.dto.split;

import com.fasterxml.jackson.annotation.JsonFormat;

import java.time.LocalDateTime;
import java.util.List;

public class GroupResponse {
    private Long id;
    private String name;
    private String description;
    private String currency;
    private Long adminUserId;
    private String adminUsername;
    private int memberCount;
    private List<GroupMemberResponse> members;

    @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss")
    private LocalDateTime createdAt;

    public GroupResponse() {}

    public GroupResponse(Long id, String name, String description, String currency,
                        Long adminUserId, String adminUsername, int memberCount,
                        List<GroupMemberResponse> members, LocalDateTime createdAt) {
        this.id = id;
        this.name = name;
        this.description = description;
        this.currency = currency;
        this.adminUserId = adminUserId;
        this.adminUsername = adminUsername;
        this.memberCount = memberCount;
        this.members = members;
        this.createdAt = createdAt;
    }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }

    public String getCurrency() { return currency; }
    public void setCurrency(String currency) { this.currency = currency; }

    public Long getAdminUserId() { return adminUserId; }
    public void setAdminUserId(Long adminUserId) { this.adminUserId = adminUserId; }

    public String getAdminUsername() { return adminUsername; }
    public void setAdminUsername(String adminUsername) { this.adminUsername = adminUsername; }

    public int getMemberCount() { return memberCount; }
    public void setMemberCount(int memberCount) { this.memberCount = memberCount; }

    public List<GroupMemberResponse> getMembers() { return members; }
    public void setMembers(List<GroupMemberResponse> members) { this.members = members; }

    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }
}