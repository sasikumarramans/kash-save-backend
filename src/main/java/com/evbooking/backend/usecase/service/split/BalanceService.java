package com.evbooking.backend.usecase.service.split;

import com.evbooking.backend.domain.model.User;
import com.evbooking.backend.domain.model.split.*;
import com.evbooking.backend.domain.repository.UserRepository;
import com.evbooking.backend.domain.repository.split.*;
import com.evbooking.backend.presentation.dto.split.*;
import com.evbooking.backend.infrastructure.mapper.split.*;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

@Service
public class BalanceService {

    private final SplitExpenseRepository splitExpenseRepository;
    private final SplitParticipantRepository splitParticipantRepository;
    private final GroupRepository groupRepository;
    private final GroupMemberRepository groupMemberRepository;
    private final UserRepository userRepository;
    private final SplitExpenseMapper splitExpenseMapper;
    private final SplitParticipantMapper splitParticipantMapper;
    private final GroupMapper groupMapper;
    private final GroupMemberMapper groupMemberMapper;

    public BalanceService(SplitExpenseRepository splitExpenseRepository,
                         SplitParticipantRepository splitParticipantRepository,
                         GroupRepository groupRepository,
                         GroupMemberRepository groupMemberRepository,
                         UserRepository userRepository,
                         SplitExpenseMapper splitExpenseMapper,
                         SplitParticipantMapper splitParticipantMapper,
                         GroupMapper groupMapper,
                         GroupMemberMapper groupMemberMapper) {
        this.splitExpenseRepository = splitExpenseRepository;
        this.splitParticipantRepository = splitParticipantRepository;
        this.groupRepository = groupRepository;
        this.groupMemberRepository = groupMemberRepository;
        this.userRepository = userRepository;
        this.splitExpenseMapper = splitExpenseMapper;
        this.splitParticipantMapper = splitParticipantMapper;
        this.groupMapper = groupMapper;
        this.groupMemberMapper = groupMemberMapper;
    }

    public List<FriendBalanceResponse> getFriendsBalances(Long userId, String filter) {
        // Get all individual (non-group) expenses where user is involved
        var expenseEntities = splitExpenseRepository.findExpensesByParticipantUserId(userId);
        List<SplitExpense> individualExpenses = expenseEntities.stream()
            .map(splitExpenseMapper::toDomain)
            .filter(expense -> expense.getGroupId() == null)
            .collect(Collectors.toList());

        // Calculate balances per friend
        Map<Long, FriendBalanceData> friendBalances = new HashMap<>();

        for (SplitExpense expense : individualExpenses) {
            var participantEntities = splitParticipantRepository.findBySplitExpenseId(expense.getId());
            List<SplitParticipant> participants = participantEntities.stream()
                .map(splitParticipantMapper::toDomain)
                .collect(Collectors.toList());

            for (SplitParticipant participant : participants) {
                if (!participant.getUserId().equals(userId)) {
                    // This is a friend
                    Long friendId = participant.getUserId();
                    FriendBalanceData balanceData = friendBalances.computeIfAbsent(friendId,
                        k -> new FriendBalanceData(friendId, expense.getCurrency()));

                    // Find current user's participation in this expense
                    Optional<SplitParticipant> currentUserParticipant = participants.stream()
                        .filter(p -> p.getUserId().equals(userId))
                        .findFirst();

                    if (currentUserParticipant.isPresent()) {
                        SplitParticipant myParticipation = currentUserParticipant.get();

                        // If friend owes money and it's not settled
                        if (!participant.isSettled() && participant.getAmountOwed().compareTo(BigDecimal.ZERO) > 0) {
                            // Check who paid for this expense
                            if (expense.getPaidByUserId().equals(userId)) {
                                // I paid, so friend owes me
                                balanceData.addOwesYou(participant.getAmountOwed());
                            } else if (expense.getPaidByUserId().equals(friendId)) {
                                // Friend paid, so I owe friend
                                balanceData.addYouOwe(myParticipation.getAmountOwed());
                            }
                        }

                        balanceData.incrementExpenseCount();
                        if (participant.isSettled() && myParticipation.isSettled()) {
                            balanceData.incrementSettledCount();
                        } else {
                            balanceData.incrementPendingCount();
                        }

                        // Update last activity
                        if (balanceData.getLastActivity() == null ||
                            expense.getCreatedAt().isAfter(balanceData.getLastActivity())) {
                            balanceData.setLastActivity(expense.getCreatedAt());
                        }
                    }
                }
            }
        }

        // Convert to response objects
        List<FriendBalanceResponse> responses = new ArrayList<>();
        for (FriendBalanceData balanceData : friendBalances.values()) {
            Optional<User> friendUser = userRepository.findById(balanceData.getFriendId());
            if (friendUser.isPresent()) {
                User friend = friendUser.get();
                BigDecimal netBalance = balanceData.getOwesYou().subtract(balanceData.getYouOwe());

                FriendBalanceResponse response = new FriendBalanceResponse(
                    friend.getId(),
                    friend.getUsername(),
                    friend.getEmail(),
                    netBalance,
                    balanceData.getYouOwe(),
                    balanceData.getOwesYou(),
                    balanceData.getCurrency(),
                    balanceData.getExpenseCount(),
                    balanceData.getSettledCount(),
                    balanceData.getPendingCount(),
                    balanceData.getLastActivity()
                );

                responses.add(response);
            }
        }

        // Apply filter
        return filterFriendBalances(responses, filter);
    }

    public List<GroupBalanceResponse> getGroupsBalances(Long userId, String filter) {
        // Get all groups where user is a member
        var membershipEntities = groupMemberRepository.findByUserId(userId);
        List<GroupMember> memberships = membershipEntities.stream()
            .map(groupMemberMapper::toDomain)
            .collect(Collectors.toList());
        List<GroupBalanceResponse> responses = new ArrayList<>();

        for (GroupMember membership : memberships) {
            var groupEntityOpt = groupRepository.findById(membership.getGroupId());
            if (groupEntityOpt.isPresent()) {
                Group group = groupMapper.toDomain(groupEntityOpt.get());

                // Calculate balance for this group
                GroupBalanceData balanceData = calculateGroupBalance(group.getId(), userId);

                // Get member count
                int memberCount = (int) groupMemberRepository.countByGroupId(group.getId());

                GroupBalanceResponse response = new GroupBalanceResponse(
                    group.getId(),
                    group.getName(),
                    group.getDescription(),
                    group.getCurrency(),
                    memberCount,
                    balanceData.getNetBalance(),
                    balanceData.getYouOwe(),
                    balanceData.getOwesYou(),
                    balanceData.getExpenseCount(),
                    balanceData.getSettledCount(),
                    balanceData.getPendingCount(),
                    membership.isAdmin(),
                    balanceData.getLastActivity()
                );

                responses.add(response);
            }
        }

        // Apply filter
        return filterGroupBalances(responses, filter);
    }

    public OverallBalanceSummaryResponse getOverallBalanceSummary(Long userId) {
        List<FriendBalanceResponse> friendsBalances = getFriendsBalances(userId, "all");
        List<GroupBalanceResponse> groupsBalances = getGroupsBalances(userId, "all");

        // Calculate totals
        BigDecimal totalYouOwe = BigDecimal.ZERO;
        BigDecimal totalOwesYou = BigDecimal.ZERO;

        // Friends totals
        int friendsYouOwe = 0;
        int friendsWhoOweYou = 0;
        int settledFriends = 0;

        for (FriendBalanceResponse friend : friendsBalances) {
            totalYouOwe = totalYouOwe.add(friend.getYouOwe());
            totalOwesYou = totalOwesYou.add(friend.getOwesYou());

            if (friend.youOweAmount()) friendsYouOwe++;
            else if (friend.owesYouAmount()) friendsWhoOweYou++;
            else settledFriends++;
        }

        // Groups totals
        int groupsYouOwe = 0;
        int groupsWhoOweYou = 0;
        int settledGroups = 0;

        for (GroupBalanceResponse group : groupsBalances) {
            totalYouOwe = totalYouOwe.add(group.getYouOwe());
            totalOwesYou = totalOwesYou.add(group.getOwesYou());

            if (group.youOweAmount()) groupsYouOwe++;
            else if (group.owesYouAmount()) groupsWhoOweYou++;
            else settledGroups++;
        }

        BigDecimal netBalance = totalOwesYou.subtract(totalYouOwe);

        // Expense totals
        var allExpenseEntities = splitExpenseRepository.findExpensesByParticipantUserId(userId);
        List<SplitExpense> allExpenses = allExpenseEntities.stream()
            .map(splitExpenseMapper::toDomain)
            .collect(Collectors.toList());
        int totalExpenses = allExpenses.size();

        int settledExpenses = 0;
        for (SplitExpense expense : allExpenses) {
            var participantEntities = splitParticipantRepository.findBySplitExpenseId(expense.getId());
            List<SplitParticipant> participants = participantEntities.stream()
                .map(splitParticipantMapper::toDomain)
                .collect(Collectors.toList());
            boolean allSettled = participants.stream().allMatch(SplitParticipant::isSettled);
            if (allSettled) settledExpenses++;
        }

        int pendingExpenses = totalExpenses - settledExpenses;

        return new OverallBalanceSummaryResponse(
            totalYouOwe,
            totalOwesYou,
            netBalance,
            "INR", // Default currency - could be made configurable
            friendsBalances.size(),
            friendsYouOwe,
            friendsWhoOweYou,
            settledFriends,
            groupsBalances.size(),
            groupsYouOwe,
            groupsWhoOweYou,
            settledGroups,
            totalExpenses,
            settledExpenses,
            pendingExpenses
        );
    }

    private GroupBalanceData calculateGroupBalance(Long groupId, Long userId) {
        // Get all expenses for this group (including related individual expenses)
        var groupMemberEntities = groupMemberRepository.findByGroupId(groupId);
        List<GroupMember> groupMembers = groupMemberEntities.stream()
            .map(groupMemberMapper::toDomain)
            .collect(Collectors.toList());
        List<Long> memberUserIds = groupMembers.stream()
            .map(GroupMember::getUserId)
            .collect(Collectors.toList());

        // This would need the enhanced repository method we discussed
        // For now, just get direct group expenses
        var groupExpenseEntities = splitExpenseRepository.findByGroupId(groupId, org.springframework.data.domain.Pageable.unpaged());
        List<SplitExpense> groupExpenses = groupExpenseEntities.getContent().stream()
            .map(splitExpenseMapper::toDomain)
            .collect(Collectors.toList());

        GroupBalanceData balanceData = new GroupBalanceData();

        for (SplitExpense expense : groupExpenses) {
            var participantEntities = splitParticipantRepository.findBySplitExpenseId(expense.getId());
            List<SplitParticipant> participants = participantEntities.stream()
                .map(splitParticipantMapper::toDomain)
                .collect(Collectors.toList());

            Optional<SplitParticipant> userParticipant = participants.stream()
                .filter(p -> p.getUserId().equals(userId))
                .findFirst();

            if (userParticipant.isPresent()) {
                SplitParticipant myParticipation = userParticipant.get();

                if (!myParticipation.isSettled()) {
                    if (expense.getPaidByUserId().equals(userId)) {
                        // I paid, others owe me
                        BigDecimal othersOweMe = participants.stream()
                            .filter(p -> !p.getUserId().equals(userId) && !p.isSettled())
                            .map(SplitParticipant::getAmountOwed)
                            .reduce(BigDecimal.ZERO, BigDecimal::add);
                        balanceData.addOwesYou(othersOweMe);
                    } else {
                        // Someone else paid, I owe them
                        balanceData.addYouOwe(myParticipation.getAmountOwed());
                    }
                }

                balanceData.incrementExpenseCount();
                if (myParticipation.isSettled()) {
                    balanceData.incrementSettledCount();
                } else {
                    balanceData.incrementPendingCount();
                }

                if (balanceData.getLastActivity() == null ||
                    expense.getCreatedAt().isAfter(balanceData.getLastActivity())) {
                    balanceData.setLastActivity(expense.getCreatedAt());
                }
            }
        }

        return balanceData;
    }

    private List<FriendBalanceResponse> filterFriendBalances(List<FriendBalanceResponse> balances, String filter) {
        if (filter == null || "all".equals(filter)) {
            return balances;
        }

        return balances.stream()
            .filter(balance -> switch (filter) {
                case "outstanding" -> balance.hasOutstandingBalance();
                case "you_owe" -> balance.youOweAmount();
                case "owes_you" -> balance.owesYouAmount();
                case "settled" -> balance.isSettled();
                default -> true;
            })
            .collect(Collectors.toList());
    }

    private List<GroupBalanceResponse> filterGroupBalances(List<GroupBalanceResponse> balances, String filter) {
        if (filter == null || "all".equals(filter)) {
            return balances;
        }

        return balances.stream()
            .filter(balance -> switch (filter) {
                case "outstanding" -> balance.hasOutstandingBalance();
                case "you_owe" -> balance.youOweAmount();
                case "owes_you" -> balance.owesYouAmount();
                case "settled" -> balance.isSettled();
                default -> true;
            })
            .collect(Collectors.toList());
    }

    // Helper classes for internal calculations
    private static class FriendBalanceData {
        private final Long friendId;
        private final String currency;
        private BigDecimal youOwe = BigDecimal.ZERO;
        private BigDecimal owesYou = BigDecimal.ZERO;
        private int expenseCount = 0;
        private int settledCount = 0;
        private int pendingCount = 0;
        private LocalDateTime lastActivity;

        public FriendBalanceData(Long friendId, String currency) {
            this.friendId = friendId;
            this.currency = currency;
        }

        // Getters and increment methods
        public Long getFriendId() { return friendId; }
        public String getCurrency() { return currency; }
        public BigDecimal getYouOwe() { return youOwe; }
        public BigDecimal getOwesYou() { return owesYou; }
        public int getExpenseCount() { return expenseCount; }
        public int getSettledCount() { return settledCount; }
        public int getPendingCount() { return pendingCount; }
        public LocalDateTime getLastActivity() { return lastActivity; }

        public void addYouOwe(BigDecimal amount) { this.youOwe = this.youOwe.add(amount); }
        public void addOwesYou(BigDecimal amount) { this.owesYou = this.owesYou.add(amount); }
        public void incrementExpenseCount() { this.expenseCount++; }
        public void incrementSettledCount() { this.settledCount++; }
        public void incrementPendingCount() { this.pendingCount++; }
        public void setLastActivity(LocalDateTime lastActivity) { this.lastActivity = lastActivity; }
    }

    private static class GroupBalanceData {
        private BigDecimal youOwe = BigDecimal.ZERO;
        private BigDecimal owesYou = BigDecimal.ZERO;
        private int expenseCount = 0;
        private int settledCount = 0;
        private int pendingCount = 0;
        private LocalDateTime lastActivity;

        public BigDecimal getYouOwe() { return youOwe; }
        public BigDecimal getOwesYou() { return owesYou; }
        public BigDecimal getNetBalance() { return owesYou.subtract(youOwe); }
        public int getExpenseCount() { return expenseCount; }
        public int getSettledCount() { return settledCount; }
        public int getPendingCount() { return pendingCount; }
        public LocalDateTime getLastActivity() { return lastActivity; }

        public void addYouOwe(BigDecimal amount) { this.youOwe = this.youOwe.add(amount); }
        public void addOwesYou(BigDecimal amount) { this.owesYou = this.owesYou.add(amount); }
        public void incrementExpenseCount() { this.expenseCount++; }
        public void incrementSettledCount() { this.settledCount++; }
        public void incrementPendingCount() { this.pendingCount++; }
        public void setLastActivity(LocalDateTime lastActivity) { this.lastActivity = lastActivity; }
    }
}