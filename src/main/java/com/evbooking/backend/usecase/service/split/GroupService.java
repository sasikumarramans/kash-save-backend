package com.evbooking.backend.usecase.service.split;

import com.evbooking.backend.domain.model.split.Group;
import com.evbooking.backend.domain.model.split.GroupMember;
import com.evbooking.backend.domain.model.split.GroupDeletionHistory;
import com.evbooking.backend.domain.model.User;
import com.evbooking.backend.domain.repository.split.GroupRepository;
import com.evbooking.backend.domain.repository.split.GroupMemberRepository;
import com.evbooking.backend.domain.repository.split.GroupDeletionHistoryRepository;
import com.evbooking.backend.domain.repository.split.SplitExpenseRepository;
import com.evbooking.backend.domain.repository.UserRepository;
import com.evbooking.backend.infrastructure.mapper.split.GroupMapper;
import com.evbooking.backend.infrastructure.mapper.split.GroupMemberMapper;
import com.evbooking.backend.infrastructure.mapper.split.GroupDeletionHistoryMapper;
import com.evbooking.backend.presentation.dto.split.GroupResponse;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
@Transactional
public class GroupService {

    private final GroupRepository groupRepository;
    private final GroupMemberRepository groupMemberRepository;
    private final GroupDeletionHistoryRepository groupDeletionHistoryRepository;
    private final SplitExpenseRepository splitExpenseRepository;
    private final UserRepository userRepository;
    private final SplitActivityService splitActivityService;
    private final GroupMapper groupMapper;
    private final GroupMemberMapper groupMemberMapper;
    private final GroupDeletionHistoryMapper groupDeletionHistoryMapper;

    public GroupService(GroupRepository groupRepository, GroupMemberRepository groupMemberRepository,
                       GroupDeletionHistoryRepository groupDeletionHistoryRepository,
                       SplitExpenseRepository splitExpenseRepository,
                       UserRepository userRepository,
                       SplitActivityService splitActivityService, GroupMapper groupMapper,
                       GroupMemberMapper groupMemberMapper, GroupDeletionHistoryMapper groupDeletionHistoryMapper) {
        this.groupRepository = groupRepository;
        this.groupMemberRepository = groupMemberRepository;
        this.groupDeletionHistoryRepository = groupDeletionHistoryRepository;
        this.splitExpenseRepository = splitExpenseRepository;
        this.userRepository = userRepository;
        this.splitActivityService = splitActivityService;
        this.groupMapper = groupMapper;
        this.groupMemberMapper = groupMemberMapper;
        this.groupDeletionHistoryMapper = groupDeletionHistoryMapper;
    }

    public Group createGroup(String name, String description, String currency, List<String> memberUsernames, String adminUserId) {
        // Validate admin user exists
        if (!userRepository.findById(adminUserId).isPresent()) {
            throw new RuntimeException("Admin user not found");
        }

        // Create group
        Group group = new Group(name, description, adminUserId, currency);
        var savedEntity = groupRepository.save(groupMapper.toEntity(group));
        group = groupMapper.toDomain(savedEntity);

        // Add admin as member
        GroupMember adminMember = new GroupMember(group.getId(), adminUserId, true);
        groupMemberRepository.save(groupMemberMapper.toEntity(adminMember));

        // Add other members by username
        for (String username : memberUsernames) {
            Optional<User> userOpt = userRepository.findByUsername(username);
            if (userOpt.isEmpty()) {
                throw new RuntimeException("User not found with username: " + username);
            }

            User user = userOpt.get();
            // Skip if admin is already added
            if (!user.getId().equals(adminUserId)) {
                // Check if user is already a member
                if (!groupMemberRepository.existsByGroupIdAndUserId(group.getId(), user.getId())) {
                    GroupMember member = new GroupMember(group.getId(), user.getId(), false);
                    groupMemberRepository.save(groupMemberMapper.toEntity(member));

                    // Log member added activity
                    splitActivityService.logMemberAdded(group.getId(), user.getId().toString(), adminUserId);
                }
            }
        }

        // Log group creation activity
        splitActivityService.logGroupCreated(group.getId(), adminUserId.toString());

        return group;
    }

    public Optional<Group> getGroupById(Long groupId, String userId) {
        // Check if user is a member of the group
        if (!groupMemberRepository.existsByGroupIdAndUserId(groupId, userId)) {
            return Optional.empty();
        }

        return groupRepository.findById(groupId).map(groupMapper::toDomain);
    }

    public Page<Group> getUserGroups(String userId, Pageable pageable) {
        var entityPage = groupRepository.findGroupsByMemberId(userId, pageable);
        var domainList = entityPage.getContent().stream()
            .map(groupMapper::toDomain)
            .collect(Collectors.toList());
        return new PageImpl<>(domainList, pageable, entityPage.getTotalElements());
    }

    public List<GroupMember> getGroupMembers(Long groupId, String userId) {
        // Check if user is a member of the group
        if (!groupMemberRepository.existsByGroupIdAndUserId(groupId, userId)) {
            throw new RuntimeException("You are not a member of this group");
        }

        var memberEntities = groupMemberRepository.findByGroupId(groupId);
        return memberEntities.stream()
            .map(groupMemberMapper::toDomain)
            .collect(Collectors.toList());
    }

    public GroupMember addMemberToGroup(Long groupId, String username, String adminUserId) {
        // Verify admin permissions
        var adminMemberEntityOpt = groupMemberRepository.findByGroupIdAndUserId(groupId, adminUserId);
        if (adminMemberEntityOpt.isEmpty() || !groupMemberMapper.toDomain(adminMemberEntityOpt.get()).isAdmin()) {
            throw new RuntimeException("Only group admins can add members");
        }

        // Find user by username
        Optional<User> userOpt = userRepository.findByUsername(username);
        if (userOpt.isEmpty()) {
            throw new RuntimeException("User not found with username: " + username);
        }

        User user = userOpt.get();

        // Check if user is already a member
        if (groupMemberRepository.existsByGroupIdAndUserId(groupId, user.getId())) {
            throw new RuntimeException("User is already a member of this group");
        }

        GroupMember member = new GroupMember(groupId, user.getId(), false);
        var savedEntity = groupMemberRepository.save(groupMemberMapper.toEntity(member));
        GroupMember savedMember = groupMemberMapper.toDomain(savedEntity);

        // Log member added activity
        splitActivityService.logMemberAdded(groupId, user.getId(), adminUserId);

        return savedMember;
    }

    public void removeMemberFromGroup(Long groupId, String userIdToRemove, String requestingUserId) {
        // Verify requesting user is a member
        var requestingMemberEntityOpt = groupMemberRepository.findByGroupIdAndUserId(groupId, requestingUserId);
        if (requestingMemberEntityOpt.isEmpty()) {
            throw new RuntimeException("You are not a member of this group");
        }

        // Check if member to remove exists
        if (!groupMemberRepository.existsByGroupIdAndUserId(groupId, userIdToRemove)) {
            throw new RuntimeException("User is not a member of this group");
        }

        // Prevent removing the last admin
        var adminEntities = groupMemberRepository.findAdminsByGroupId(groupId);
        List<GroupMember> admins = adminEntities.stream()
            .map(groupMemberMapper::toDomain)
            .collect(Collectors.toList());
        if (admins.size() == 1 && admins.get(0).getUserId().equals(userIdToRemove)) {
            throw new RuntimeException("Cannot remove the last admin from the group");
        }

        groupMemberRepository.deleteByGroupIdAndUserId(groupId, userIdToRemove);

        // Log member removal activity
        splitActivityService.logMemberRemoved(groupId, userIdToRemove, requestingUserId);
    }

    public void leaveGroup(Long groupId, String userId) {
        // Check if user is a member
        if (!groupMemberRepository.existsByGroupIdAndUserId(groupId, userId)) {
            throw new RuntimeException("You are not a member of this group");
        }

        // Check if user is the last admin
        var adminEntities = groupMemberRepository.findAdminsByGroupId(groupId);
        List<GroupMember> admins = adminEntities.stream()
            .map(groupMemberMapper::toDomain)
            .collect(Collectors.toList());
        boolean isLastAdmin = admins.size() == 1 && admins.get(0).getUserId().equals(userId);

        if (isLastAdmin) {
            // Check if there are other members
            long memberCount = groupMemberRepository.countByGroupId(groupId);
            if (memberCount > 1) {
                throw new RuntimeException("Cannot leave group as the last admin. Please transfer admin rights first or delete the group.");
            } else {
                // Delete the entire group if last member
                deleteGroup(groupId, userId);
                return;
            }
        }

        groupMemberRepository.deleteByGroupIdAndUserId(groupId, userId);

        // Log member left activity
        splitActivityService.logMemberLeft(groupId, userId);
    }

    public void deleteGroup(Long groupId, String userId) {
        // Verify user is a member
        var memberEntityOpt = groupMemberRepository.findByGroupIdAndUserId(groupId, userId);
        if (memberEntityOpt.isEmpty()) {
            throw new RuntimeException("You are not a member of this group");
        }

        // Check if group exists
        var groupEntityOpt = groupRepository.findById(groupId);
        if (groupEntityOpt.isEmpty()) {
            throw new RuntimeException("Group not found");
        }

        Group group = groupMapper.toDomain(groupEntityOpt.get());

        // Get user who is deleting
        Optional<User> userOpt = userRepository.findById(userId);
        String username = userOpt.map(User::getUsername).orElse("Unknown");

        // Count members before deletion
        int memberCount = (int) groupMemberRepository.countByGroupId(groupId);

        // Record deletion history
        GroupDeletionHistory deletionHistory = new GroupDeletionHistory(
            groupId,
            group.getName(),
            group.getDescription(),
            userId,
            username,
            memberCount,
            "Group deleted by member"
        );
        groupDeletionHistoryRepository.save(groupDeletionHistoryMapper.toEntity(deletionHistory));

        // Log group deletion activity
        splitActivityService.logGroupDeleted(groupId, userId, group.getName());

        // Delete the group (cascades to members)
        groupRepository.deleteById(groupId);
    }

    public GroupMember makeAdmin(Long groupId, String userIdToPromote, String currentAdminUserId) {
        // Verify current admin permissions
        var currentAdminEntityOpt = groupMemberRepository.findByGroupIdAndUserId(groupId, currentAdminUserId);
        if (currentAdminEntityOpt.isEmpty() || !groupMemberMapper.toDomain(currentAdminEntityOpt.get()).isAdmin()) {
            throw new RuntimeException("Only group admins can promote members");
        }

        // Find member to promote
        var memberEntityOpt = groupMemberRepository.findByGroupIdAndUserId(groupId, userIdToPromote);
        if (memberEntityOpt.isEmpty()) {
            throw new RuntimeException("User is not a member of this group");
        }

        GroupMember member = groupMemberMapper.toDomain(memberEntityOpt.get());
        if (member.isAdmin()) {
            throw new RuntimeException("User is already an admin");
        }

        member.setAdmin(true);
        var savedEntity = groupMemberRepository.save(groupMemberMapper.toEntity(member));
        return groupMemberMapper.toDomain(savedEntity);
    }

    public Group updateGroup(Long groupId, String name, String description, String currency, String adminUserId) {
        // Verify admin permissions
        var adminMemberEntityOpt = groupMemberRepository.findByGroupIdAndUserId(groupId, adminUserId);
        if (adminMemberEntityOpt.isEmpty() || !groupMemberMapper.toDomain(adminMemberEntityOpt.get()).isAdmin()) {
            throw new RuntimeException("Only group admins can update the group");
        }

        var groupEntityOpt = groupRepository.findById(groupId);
        if (groupEntityOpt.isEmpty()) {
            throw new RuntimeException("Group not found");
        }

        Group group = groupMapper.toDomain(groupEntityOpt.get());
        if (name != null && !name.trim().isEmpty()) {
            group.setName(name.trim());
        }
        if (description != null) {
            group.setDescription(description);
        }
        if (currency != null && !currency.trim().isEmpty()) {
            group.setCurrency(currency.trim());
        }

        var savedEntity = groupRepository.save(groupMapper.toEntity(group));
        return groupMapper.toDomain(savedEntity);
    }

    public boolean isGroupMember(Long groupId, String userId) {
        return groupMemberRepository.existsByGroupIdAndUserId(groupId, userId);
    }

    public boolean isGroupAdmin(Long groupId, String userId) {
        var memberEntityOpt = groupMemberRepository.findByGroupIdAndUserId(groupId, userId);
        return memberEntityOpt.isPresent() && groupMemberMapper.toDomain(memberEntityOpt.get()).isAdmin();
    }

    /**
     * Get user groups including virtual groups for each non-group expense
     * Each non-group expense appears as a separate "Non-Group Expenses" entry
     */
    public Page<GroupResponse> getUserGroupsWithFriends(String userId, Pageable pageable) {
        // Get regular groups
        Page<Group> groupsPage = getUserGroups(userId, pageable);
        List<GroupResponse> allGroups = new ArrayList<>();

        // Get all non-group expenses for this user
        Pageable nonGroupPageable = PageRequest.of(0, 1000); // Get all non-group expenses
        var nonGroupExpensesPage = splitExpenseRepository.findByGroupIdIsNull(userId, nonGroupPageable);

        // Get current user details
        Optional<User> currentUserOpt = userRepository.findById(userId);
        String username = currentUserOpt.map(User::getUsername).orElse("You");

        // Create a virtual group for EACH non-group expense
        nonGroupExpensesPage.getContent().forEach(expenseEntity -> {
            GroupResponse nonGroupExpenseGroup = new GroupResponse(
                expenseEntity.getId(), // Use expense ID directly
                "expense", // Type: expense (not a real group)
                "Non-Group Expenses",
                expenseEntity.getDescription(), // Use expense description
                expenseEntity.getCurrency(),
                userId,
                username,
                0, // Member count will be calculated separately if needed
                null, // No members list
                expenseEntity.getCreatedAt()
            );
            allGroups.add(nonGroupExpenseGroup);
        });

        // Add regular groups
        groupsPage.getContent().forEach(group -> {
            // Get user details for admin
            Optional<User> adminUserOpt = userRepository.findById(group.getAdminUserId());
            String adminUsername = adminUserOpt.map(User::getUsername).orElse("Unknown");

            // Get member count
            long memberCount = groupMemberRepository.countByGroupId(group.getId());

            GroupResponse response = new GroupResponse(
                group.getId(),
                "group", // Type: group (real group)
                group.getName(),
                group.getDescription(),
                group.getCurrency(),
                group.getAdminUserId(),
                adminUsername,
                (int) memberCount,
                null, // Members list will be populated separately if needed
                group.getCreatedAt()
            );
            allGroups.add(response);
        });

        // Calculate total elements (non-group expenses + regular groups)
        long totalElements = groupsPage.getTotalElements() + nonGroupExpensesPage.getTotalElements();

        return new PageImpl<>(allGroups, pageable, totalElements);
    }
}