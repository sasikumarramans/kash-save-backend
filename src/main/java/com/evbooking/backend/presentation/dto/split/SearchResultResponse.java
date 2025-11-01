package com.evbooking.backend.presentation.dto.split;

import java.util.List;

/**
 * Unified search response containing both friends and groups
 */
public class SearchResultResponse {
    private List<FriendSearchResult> friends;
    private List<GroupSearchResult> groups;
    private int totalFriends;
    private int totalGroups;

    public SearchResultResponse() {}

    public SearchResultResponse(List<FriendSearchResult> friends, List<GroupSearchResult> groups,
                               int totalFriends, int totalGroups) {
        this.friends = friends;
        this.groups = groups;
        this.totalFriends = totalFriends;
        this.totalGroups = totalGroups;
    }

    // Getters and setters
    public List<FriendSearchResult> getFriends() {
        return friends;
    }

    public void setFriends(List<FriendSearchResult> friends) {
        this.friends = friends;
    }

    public List<GroupSearchResult> getGroups() {
        return groups;
    }

    public void setGroups(List<GroupSearchResult> groups) {
        this.groups = groups;
    }

    public int getTotalFriends() {
        return totalFriends;
    }

    public void setTotalFriends(int totalFriends) {
        this.totalFriends = totalFriends;
    }

    public int getTotalGroups() {
        return totalGroups;
    }

    public void setTotalGroups(int totalGroups) {
        this.totalGroups = totalGroups;
    }

    /**
     * Friend search result item
     */
    public static class FriendSearchResult {
        private String userId;
        private String username;
        private String name;
        private String email;
        private String lastActivity; // Description of last collaboration

        public FriendSearchResult() {}

        public FriendSearchResult(String userId, String username, String name, String email, String lastActivity) {
            this.userId = userId;
            this.username = username;
            this.name = name;
            this.email = email;
            this.lastActivity = lastActivity;
        }

        public String getUserId() {
            return userId;
        }

        public void setUserId(String userId) {
            this.userId = userId;
        }

        public String getUsername() {
            return username;
        }

        public void setUsername(String username) {
            this.username = username;
        }

        public String getName() {
            return name;
        }

        public void setName(String name) {
            this.name = name;
        }

        public String getEmail() {
            return email;
        }

        public void setEmail(String email) {
            this.email = email;
        }

        public String getLastActivity() {
            return lastActivity;
        }

        public void setLastActivity(String lastActivity) {
            this.lastActivity = lastActivity;
        }
    }

    /**
     * Group search result item
     */
    public static class GroupSearchResult {
        private Long groupId;
        private String groupName;
        private String description;
        private int memberCount;
        private String lastActivity; // Description of last expense

        public GroupSearchResult() {}

        public GroupSearchResult(Long groupId, String groupName, String description,
                                int memberCount, String lastActivity) {
            this.groupId = groupId;
            this.groupName = groupName;
            this.description = description;
            this.memberCount = memberCount;
            this.lastActivity = lastActivity;
        }

        public Long getGroupId() {
            return groupId;
        }

        public void setGroupId(Long groupId) {
            this.groupId = groupId;
        }

        public String getGroupName() {
            return groupName;
        }

        public void setGroupName(String groupName) {
            this.groupName = groupName;
        }

        public String getDescription() {
            return description;
        }

        public void setDescription(String description) {
            this.description = description;
        }

        public int getMemberCount() {
            return memberCount;
        }

        public void setMemberCount(int memberCount) {
            this.memberCount = memberCount;
        }

        public String getLastActivity() {
            return lastActivity;
        }

        public void setLastActivity(String lastActivity) {
            this.lastActivity = lastActivity;
        }
    }
}