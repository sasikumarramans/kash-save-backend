package com.evbooking.backend.presentation.controller.split;

import com.evbooking.backend.domain.model.User;
import com.evbooking.backend.domain.model.split.SplitExpense;
import com.evbooking.backend.domain.model.split.SplitParticipant;
import com.evbooking.backend.domain.repository.UserRepository;
import com.evbooking.backend.domain.repository.split.GroupRepository;
import com.evbooking.backend.domain.repository.split.SplitExpenseRepository;
import com.evbooking.backend.domain.repository.split.SplitParticipantRepository;
import com.evbooking.backend.infrastructure.entity.split.GroupEntity;
import com.evbooking.backend.infrastructure.mapper.split.SplitExpenseMapper;
import com.evbooking.backend.infrastructure.mapper.split.SplitParticipantMapper;
import com.evbooking.backend.presentation.dto.ApiResponse;
import com.evbooking.backend.presentation.dto.split.FriendsListResponse;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/friends")
public class FriendsController {

    private final UserRepository userRepository;
    private final SplitExpenseRepository splitExpenseRepository;
    private final SplitParticipantRepository splitParticipantRepository;
    private final GroupRepository groupRepository;
    private final SplitExpenseMapper splitExpenseMapper;
    private final SplitParticipantMapper splitParticipantMapper;

    public FriendsController(UserRepository userRepository,
                            SplitExpenseRepository splitExpenseRepository,
                            SplitParticipantRepository splitParticipantRepository,
                            GroupRepository groupRepository,
                            SplitExpenseMapper splitExpenseMapper,
                            SplitParticipantMapper splitParticipantMapper) {
        this.userRepository = userRepository;
        this.splitExpenseRepository = splitExpenseRepository;
        this.splitParticipantRepository = splitParticipantRepository;
        this.groupRepository = groupRepository;
        this.splitExpenseMapper = splitExpenseMapper;
        this.splitParticipantMapper = splitParticipantMapper;
    }

    /**
     * Get friends list with their latest 2 expenses
     * Friends are expense collaborators from both group and non-group expenses
     */
    @GetMapping
    public ResponseEntity<ApiResponse<Page<FriendsListResponse>>> getFriends(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size,
            HttpServletRequest httpRequest) {
        try {
            String userId = (String) httpRequest.getAttribute("userId");
            if (userId == null) {
                return ResponseEntity.badRequest()
                    .body(ApiResponse.error("User not authenticated"));
            }

            // Get all expenses where user is involved
            var expenseEntities = splitExpenseRepository.findExpensesByParticipantUserId(userId);
            List<SplitExpense> allExpenses = expenseEntities.stream()
                .map(splitExpenseMapper::toDomain)
                .sorted((e1, e2) -> e2.getCreatedAt().compareTo(e1.getCreatedAt()))
                .collect(Collectors.toList());

            // Map to track friends and their expenses
            Map<String, FriendExpenses> friendExpensesMap = new LinkedHashMap<>();

            for (SplitExpense expense : allExpenses) {
                var participantEntities = splitParticipantRepository.findBySplitExpenseId(expense.getId());

                for (var pEntity : participantEntities) {
                    SplitParticipant participant = splitParticipantMapper.toDomain(pEntity);

                    if (!participant.getUserId().equals(userId)) {
                        // This is a friend
                        String friendId = participant.getUserId();

                        friendExpensesMap.putIfAbsent(friendId, new FriendExpenses(friendId));

                        // Add ALL expenses for this friend (for calculating overall amounts)
                        friendExpensesMap.get(friendId).expenses.add(expense);
                    }
                }
            }

            // Convert to response list
            List<FriendsListResponse> allFriends = new ArrayList<>();

            for (FriendExpenses friendExpenses : friendExpensesMap.values()) {
                Optional<User> friendUserOpt = userRepository.findById(friendExpenses.friendId);

                if (friendUserOpt.isPresent()) {
                    User friend = friendUserOpt.get();
                    String fullName = (friend.getFirstName() != null ? friend.getFirstName() : "") +
                                     (friend.getLastName() != null ? " " + friend.getLastName() : "");

                    // Calculate overall paying and receiving amounts from ALL expenses
                    BigDecimal overallPayingAmount = BigDecimal.ZERO;
                    BigDecimal overallReceivingAmount = BigDecimal.ZERO;

                    // Build expense details for this friend (only latest 2)
                    List<FriendsListResponse.RecentExpenseDetail> expenseDetails = new ArrayList<>();
                    int expenseCount = 0;

                    for (SplitExpense expense : friendExpenses.expenses) {
                        FriendsListResponse.RecentExpenseDetail detail = buildExpenseDetail(
                            expense, userId, friendExpenses.friendId
                        );

                        // Add to overall amounts
                        if ("You pay".equals(detail.getStatus())) {
                            overallPayingAmount = overallPayingAmount.add(detail.getYourAmount());
                        } else if ("You receive".equals(detail.getStatus())) {
                            overallReceivingAmount = overallReceivingAmount.add(detail.getYourAmount());
                        }

                        // Only include latest 2 in the response
                        if (expenseCount < 2) {
                            expenseDetails.add(detail);
                            expenseCount++;
                        }
                    }

                    allFriends.add(new FriendsListResponse(
                        friend.getId(),
                        friend.getUsername(),
                        fullName.trim(),
                        friend.getEmail(),
                        overallPayingAmount,
                        overallReceivingAmount,
                        expenseDetails
                    ));
                }
            }

            // Apply pagination
            Pageable pageable = PageRequest.of(page, size);
            int start = (int) pageable.getOffset();
            int end = Math.min((start + pageable.getPageSize()), allFriends.size());

            List<FriendsListResponse> pagedFriends = start >= allFriends.size() ?
                new ArrayList<>() : allFriends.subList(start, end);

            Page<FriendsListResponse> friendsPage = new PageImpl<>(
                pagedFriends, pageable, allFriends.size()
            );

            return ResponseEntity.ok(ApiResponse.success(friendsPage));

        } catch (Exception e) {
            return ResponseEntity.badRequest()
                .body(ApiResponse.error(e.getMessage()));
        }
    }

    /**
     * Build expense detail for a specific expense
     */
    private FriendsListResponse.RecentExpenseDetail buildExpenseDetail(
            SplitExpense expense, String currentUserId, String friendId) {

        // Get group name if this is a group expense
        String groupName = null;
        if (expense.getGroupId() != null) {
            Optional<GroupEntity> groupOpt = groupRepository.findById(expense.getGroupId());
            if (groupOpt.isPresent()) {
                groupName = groupOpt.get().getName();
            }
        }

        // Get participants to calculate amounts
        var participantEntities = splitParticipantRepository.findBySplitExpenseId(expense.getId());

        BigDecimal yourAmount = BigDecimal.ZERO;
        String status = "";

        for (var pEntity : participantEntities) {
            SplitParticipant participant = splitParticipantMapper.toDomain(pEntity);

            if (participant.getUserId().equals(currentUserId)) {
                // Calculate what current user owes or receives
                if (expense.getPaidByUserId().equals(currentUserId)) {
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

        return new FriendsListResponse.RecentExpenseDetail(
            expense.getId(),
            expense.getGroupId(),
            groupName,
            expense.getDescription(),
            expense.getTotalAmount(),
            expense.getCurrency(),
            yourAmount,
            status,
            expense.getCreatedAt()
        );
    }

    /**
     * Helper class to track friend and their expenses
     */
    private static class FriendExpenses {
        String friendId;
        List<SplitExpense> expenses;

        FriendExpenses(String friendId) {
            this.friendId = friendId;
            this.expenses = new ArrayList<>();
        }
    }
}