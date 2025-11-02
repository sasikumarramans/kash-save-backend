package com.evbooking.backend.usecase.service.split;

import com.evbooking.backend.domain.model.split.Group;
import com.evbooking.backend.domain.model.split.GroupMember;
import com.evbooking.backend.domain.model.split.GroupDeletionHistory;
import com.evbooking.backend.domain.model.split.SplitExpense;
import com.evbooking.backend.domain.model.split.SplitParticipant;
import com.evbooking.backend.domain.model.User;
import com.evbooking.backend.domain.repository.split.GroupRepository;
import com.evbooking.backend.domain.repository.split.GroupMemberRepository;
import com.evbooking.backend.domain.repository.split.GroupDeletionHistoryRepository;
import com.evbooking.backend.domain.repository.split.SplitExpenseRepository;
import com.evbooking.backend.domain.repository.split.SplitParticipantRepository;
import com.evbooking.backend.domain.repository.UserRepository;
import com.evbooking.backend.infrastructure.mapper.split.GroupMapper;
import com.evbooking.backend.infrastructure.mapper.split.GroupMemberMapper;
import com.evbooking.backend.infrastructure.mapper.split.GroupDeletionHistoryMapper;
import com.evbooking.backend.infrastructure.mapper.split.SplitExpenseMapper;
import com.evbooking.backend.infrastructure.mapper.split.SplitParticipantMapper;
import com.evbooking.backend.presentation.dto.split.GroupResponse;
import com.evbooking.backend.presentation.dto.split.RecentExpenseResponse;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
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
    private final SplitParticipantRepository splitParticipantRepository;
    private final UserRepository userRepository;
    private final SplitActivityService splitActivityService;
    private final GroupMapper groupMapper;
    private final GroupMemberMapper groupMemberMapper;
    private final GroupDeletionHistoryMapper groupDeletionHistoryMapper;
    private final SplitExpenseMapper splitExpenseMapper;
    private final SplitParticipantMapper splitParticipantMapper;

    public GroupService(GroupRepository groupRepository, GroupMemberRepository groupMemberRepository,
                       GroupDeletionHistoryRepository groupDeletionHistoryRepository,
                       SplitExpenseRepository splitExpenseRepository,
                       SplitParticipantRepository splitParticipantRepository,
                       UserRepository userRepository,
                       SplitActivityService splitActivityService, GroupMapper groupMapper,
                       GroupMemberMapper groupMemberMapper, GroupDeletionHistoryMapper groupDeletionHistoryMapper,
                       SplitExpenseMapper splitExpenseMapper, SplitParticipantMapper splitParticipantMapper) {
        this.groupRepository = groupRepository;
        this.groupMemberRepository = groupMemberRepository;
        this.groupDeletionHistoryRepository = groupDeletionHistoryRepository;
        this.splitExpenseRepository = splitExpenseRepository;
        this.splitParticipantRepository = splitParticipantRepository;
        this.userRepository = userRepository;
        this.splitActivityService = splitActivityService;
        this.groupMapper = groupMapper;
        this.groupMemberMapper = groupMemberMapper;
        this.groupDeletionHistoryMapper = groupDeletionHistoryMapper;
        this.splitExpenseMapper = splitExpenseMapper;
        this.splitParticipantMapper = splitParticipantMapper;
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
     * Get user groups including ONE virtual "Non-Group Expenses" entry for all non-group expenses
     */
    public Page<GroupResponse> getUserGroupsWithFriends(String userId, Pageable pageable) {
        // Get regular groups
        Page<Group> groupsPage = getUserGroups(userId, pageable);
        List<GroupResponse> allGroups = new ArrayList<>();

        // Count non-group expenses for this user
        Pageable countPageable = PageRequest.of(0, 1);
        var nonGroupExpensesPage = splitExpenseRepository.findByGroupIdIsNull(userId, countPageable);
        long nonGroupExpenseCount = nonGroupExpensesPage.getTotalElements();

        // Only add ONE virtual "Non-Group Expenses" entry if there are any non-group expenses
        if (nonGroupExpenseCount > 0) {
            // Get current user details
            Optional<User> currentUserOpt = userRepository.findById(userId);
            String username = currentUserOpt.map(User::getUsername).orElse("You");

            // Create ONE virtual "Non-Group Expenses" entry with special ID 0
            GroupResponse nonGroupExpensesGroup = new GroupResponse(
                0L, // Special ID 0 for virtual Non-Group Expenses
                "expense", // Type: expense (not a real group)
                "Non-Group Expenses",
                "Expenses split with friends without creating a group",
                "INR", // Default currency
                userId,
                username,
                (int) nonGroupExpenseCount, // Member count shows number of expenses
                null, // No members list
                LocalDateTime.now()
            );
            allGroups.add(nonGroupExpensesGroup);
        }

        // Add regular groups
        groupsPage.getContent().forEach(group -> {
            // Get user details for admin
            Optional<User> adminUserOpt = userRepository.findById(group.getAdminUserId());
            String adminUsername = adminUserOpt.map(User::getUsername).orElse("Unknown");

            // Get member count
            long memberCount = groupMemberRepository.countByGroupId(group.getId());

            // Calculate balance for this group
            BalanceData balanceData = calculateGroupBalance(group.getId(), userId);

            // Get recent expenses (last 2) with user-specific status and amount
            List<RecentExpenseResponse> recentExpenses = getRecentExpensesForGroup(group.getId(), userId, 2);

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

            // Set the new fields
            response.setOverallReceivingAmount(balanceData.getOwesYou());
            response.setOverallPayingAmount(balanceData.getYouOwe());
            response.setRecentExpenses(recentExpenses);

            allGroups.add(response);
        });

        // Calculate total elements (1 for non-group expenses if exists + regular groups)
        long totalElements = groupsPage.getTotalElements() + (nonGroupExpenseCount > 0 ? 1 : 0);

        return new PageImpl<>(allGroups, pageable, totalElements);
    }

    /**
     * Calculate balance data for a group from the perspective of the given user
     * Logic matches the friends API: if you paid 1000 and your share is 500, you receive 500
     */
    private BalanceData calculateGroupBalance(Long groupId, String userId) {
        // Get all expenses for this group
        var groupExpenseEntities = splitExpenseRepository.findByGroupId(groupId);
        List<SplitExpense> groupExpenses = groupExpenseEntities.stream()
            .map(splitExpenseMapper::toDomain)
            .collect(Collectors.toList());

        BalanceData balanceData = new BalanceData();

        for (SplitExpense expense : groupExpenses) {
            var participantEntities = splitParticipantRepository.findBySplitExpenseId(expense.getId());
            List<SplitParticipant> participants = participantEntities.stream()
                .map(splitParticipantMapper::toDomain)
                .collect(Collectors.toList());

            // Find current user's participation
            Optional<SplitParticipant> userParticipantOpt = participants.stream()
                .filter(p -> p.getUserId().equals(userId))
                .findFirst();

            if (userParticipantOpt.isPresent()) {
                SplitParticipant userParticipant = userParticipantOpt.get();

                if (expense.getPaidByUserId().equals(userId)) {
                    // Current user paid: they should receive (total - their share)
                    BigDecimal amountToReceive = expense.getTotalAmount().subtract(userParticipant.getAmountOwed());
                    balanceData.addOwesYou(amountToReceive);
                } else {
                    // Current user didn't pay: they owe their share
                    balanceData.addYouOwe(userParticipant.getAmountOwed());
                }
            }
        }

        return balanceData;
    }

    /**
     * Get the N most recent expenses for a group with status and amount from user's perspective
     * Logic matches the friends API implementation
     */
    private List<RecentExpenseResponse> getRecentExpensesForGroup(Long groupId, String userId, int limit) {
        // Get recent expenses ordered by created date desc
        var expenseEntities = splitExpenseRepository.findByGroupId(groupId);

        return expenseEntities.stream()
            .map(splitExpenseMapper::toDomain)
            .sorted((e1, e2) -> e2.getCreatedAt().compareTo(e1.getCreatedAt())) // Sort by created date DESC
            .limit(limit)
            .map(expense -> {
                // Get user who paid for this expense
                Optional<User> paidByUser = userRepository.findById(expense.getPaidByUserId());
                String paidByUsername = paidByUser.map(User::getUsername).orElse("Unknown");

                // Calculate status and yourAmount from current user's perspective
                BigDecimal yourAmount = BigDecimal.ZERO;
                String status = "";

                var participantEntities = splitParticipantRepository.findBySplitExpenseId(expense.getId());
                for (var pEntity : participantEntities) {
                    SplitParticipant participant = splitParticipantMapper.toDomain(pEntity);

                    if (participant.getUserId().equals(userId)) {
                        // Calculate what current user owes or receives
                        if (expense.getPaidByUserId().equals(userId)) {
                            // Current user paid: they should receive (total - their share)
                            yourAmount = expense.getTotalAmount().subtract(participant.getAmountOwed());
                            status = "You receive";
                        } else {
                            // Current user didn't pay: they owe their share
                            yourAmount = participant.getAmountOwed();
                            status = "You pay";
                        }
                        break;
                    }
                }

                return new RecentExpenseResponse(
                    expense.getId(),
                    expense.getDescription(),
                    expense.getTotalAmount(),
                    expense.getCurrency(),
                    paidByUsername,
                    expense.getPaidByUserId(),
                    yourAmount,
                    status,
                    expense.getCreatedAt()
                );
            })
            .collect(Collectors.toList());
    }

    /**
     * Helper class to track balance data for a group
     */
    private static class BalanceData {
        private BigDecimal youOwe = BigDecimal.ZERO;
        private BigDecimal owesYou = BigDecimal.ZERO;

        public BigDecimal getYouOwe() { return youOwe; }
        public BigDecimal getOwesYou() { return owesYou; }

        public void addYouOwe(BigDecimal amount) { this.youOwe = this.youOwe.add(amount); }
        public void addOwesYou(BigDecimal amount) { this.owesYou = this.owesYou.add(amount); }
    }
}