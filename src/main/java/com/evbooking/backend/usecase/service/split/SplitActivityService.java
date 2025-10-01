package com.evbooking.backend.usecase.service.split;

import com.evbooking.backend.domain.model.User;
import com.evbooking.backend.domain.model.split.*;
import com.evbooking.backend.domain.repository.UserRepository;
import com.evbooking.backend.domain.repository.split.*;
import com.evbooking.backend.presentation.dto.split.SplitActivityResponse;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

@Service
public class SplitActivityService {

    private final SplitActivityRepository splitActivityRepository;
    private final UserRepository userRepository;
    private final GroupRepository groupRepository;
    private final SplitExpenseRepository splitExpenseRepository;
    private final GroupMemberRepository groupMemberRepository;
    private final ObjectMapper objectMapper;

    public SplitActivityService(SplitActivityRepository splitActivityRepository,
                               UserRepository userRepository,
                               GroupRepository groupRepository,
                               SplitExpenseRepository splitExpenseRepository,
                               GroupMemberRepository groupMemberRepository,
                               ObjectMapper objectMapper) {
        this.splitActivityRepository = splitActivityRepository;
        this.userRepository = userRepository;
        this.groupRepository = groupRepository;
        this.splitExpenseRepository = splitExpenseRepository;
        this.groupMemberRepository = groupMemberRepository;
        this.objectMapper = objectMapper;
    }

    // ===== ACTIVITY LOGGING METHODS =====

    public void logGroupCreated(Long groupId, Long creatorUserId) {
        Optional<Group> groupOpt = groupRepository.findById(groupId);
        if (groupOpt.isPresent()) {
            Group group = groupOpt.get();
            Map<String, Object> data = Map.of(
                "groupName", group.getName(),
                "description", group.getDescription() != null ? group.getDescription() : "",
                "currency", group.getCurrency()
            );

            SplitActivity activity = new SplitActivity(
                creatorUserId,
                SplitActivityType.GROUP_CREATED,
                toJson(data),
                null,
                groupId,
                null
            );

            splitActivityRepository.save(activity);
        }
    }

    public void logMemberAdded(Long groupId, Long addedUserId, Long adminUserId) {
        Map<String, Object> data = Map.of(
            "addedUserId", addedUserId,
            "adminUserId", adminUserId
        );

        SplitActivity activity = new SplitActivity(
            adminUserId,
            SplitActivityType.MEMBER_ADDED,
            toJson(data),
            addedUserId,
            groupId,
            null
        );

        splitActivityRepository.save(activity);
    }

    public void logExpenseCreated(Long expenseId, Long creatorUserId) {
        Optional<SplitExpense> expenseOpt = splitExpenseRepository.findById(expenseId);
        if (expenseOpt.isPresent()) {
            SplitExpense expense = expenseOpt.get();
            Map<String, Object> data = Map.of(
                "description", expense.getDescription(),
                "amount", expense.getTotalAmount(),
                "currency", expense.getCurrency(),
                "splitType", expense.getSplitType().name(),
                "paidByUserId", expense.getPaidByUserId()
            );

            SplitActivity activity = new SplitActivity(
                creatorUserId,
                SplitActivityType.EXPENSE_CREATED,
                toJson(data),
                null,
                expense.getGroupId(),
                expenseId
            );

            splitActivityRepository.save(activity);
        }
    }

    public void logSettlementRecorded(Long fromUserId, Long toUserId, BigDecimal amount, String currency, Long expenseId) {
        Map<String, Object> data = Map.of(
            "amount", amount,
            "currency", currency,
            "expenseId", expenseId != null ? expenseId : ""
        );

        SplitActivity activity = new SplitActivity(
            fromUserId,
            SplitActivityType.SETTLEMENT_RECORDED,
            toJson(data),
            toUserId,
            null,
            expenseId
        );

        splitActivityRepository.save(activity);
    }

    public void logParticipantSettled(Long expenseId, Long participantUserId, Long updatedByUserId, boolean isSettled) {
        Map<String, Object> data = Map.of(
            "participantUserId", participantUserId,
            "isSettled", isSettled
        );

        SplitActivity activity = new SplitActivity(
            updatedByUserId,
            isSettled ? SplitActivityType.PARTICIPANT_SETTLED : SplitActivityType.PARTICIPANT_UNSETTLED,
            toJson(data),
            participantUserId,
            null,
            expenseId
        );

        splitActivityRepository.save(activity);
    }

    // ===== ACTIVITY RETRIEVAL METHODS =====

    public Page<SplitActivityResponse> getActivitiesForUser(Long userId, String type, Pageable pageable) {
        Page<SplitActivity> activities;

        if (type != null && !type.equals("all")) {
            switch (type) {
                case "groups" -> activities = getGroupActivitiesForUser(userId, pageable);
                case "expenses" -> activities = getExpenseActivitiesForUser(userId, pageable);
                case "payments" -> activities = getPaymentActivitiesForUser(userId, pageable);
                case "friends" -> activities = getFriendActivitiesForUser(userId, pageable);
                default -> activities = splitActivityRepository.findActivitiesForUser(userId, pageable);
            }
        } else {
            activities = splitActivityRepository.findActivitiesForUser(userId, pageable);
        }

        return activities.map(this::convertToActivityResponse);
    }

    public Page<SplitActivityResponse> getGroupActivities(Long groupId, Long userId, Pageable pageable) {
        // Verify user is group member
        if (!groupMemberRepository.existsByGroupIdAndUserId(groupId, userId)) {
            throw new RuntimeException("You are not a member of this group");
        }

        Page<SplitActivity> activities = splitActivityRepository.findByGroupIdAndUser(groupId, userId, pageable);
        return activities.map(this::convertToActivityResponse);
    }

    public Page<SplitActivityResponse> getFriendActivities(Long userId, Long friendId, Pageable pageable) {
        Page<SplitActivity> activities = splitActivityRepository.findFriendActivitiesForUser(userId, friendId, pageable);
        return activities.map(this::convertToActivityResponse);
    }

    public List<SplitActivityResponse> getRecentActivities(Long userId, int limit) {
        List<SplitActivity> activities = splitActivityRepository.findRecentActivitiesForUser(userId, limit);
        return activities.stream()
            .map(this::convertToActivityResponse)
            .collect(Collectors.toList());
    }

    // ===== PRIVATE HELPER METHODS =====

    private Page<SplitActivity> getGroupActivitiesForUser(Long userId, Pageable pageable) {
        // Get activities related to groups where user is a member
        List<GroupMember> memberships = groupMemberRepository.findByUserId(userId);
        List<Long> groupIds = memberships.stream()
            .map(GroupMember::getGroupId)
            .collect(Collectors.toList());

        // This would need a custom repository method to filter by group IDs
        return splitActivityRepository.findActivitiesForUser(userId, pageable);
    }

    private Page<SplitActivity> getExpenseActivitiesForUser(Long userId, Pageable pageable) {
        return splitActivityRepository.findByActivityTypeAndUser(SplitActivityType.EXPENSE_CREATED, userId, pageable);
    }

    private Page<SplitActivity> getPaymentActivitiesForUser(Long userId, Pageable pageable) {
        return splitActivityRepository.findByActivityTypeAndUser(SplitActivityType.SETTLEMENT_RECORDED, userId, pageable);
    }

    private Page<SplitActivity> getFriendActivitiesForUser(Long userId, Pageable pageable) {
        return splitActivityRepository.findByActivityTypeAndUser(SplitActivityType.FRIEND_ADDED, userId, pageable);
    }

    private SplitActivityResponse convertToActivityResponse(SplitActivity activity) {
        // Get actor details
        SplitActivityResponse.ActivityActor actor = null;
        Optional<User> actorUser = userRepository.findById(activity.getUserId());
        if (actorUser.isPresent()) {
            User user = actorUser.get();
            actor = new SplitActivityResponse.ActivityActor(user.getId(), user.getUsername(), user.getFullName());
        }

        // Get target details (if applicable)
        SplitActivityResponse.ActivityTarget target = null;
        if (activity.getRelatedUserId() != null) {
            Optional<User> targetUser = userRepository.findById(activity.getRelatedUserId());
            if (targetUser.isPresent()) {
                User user = targetUser.get();
                target = new SplitActivityResponse.ActivityTarget(user.getId(), user.getUsername(), user.getFullName());
            }
        }

        // Get context details
        SplitActivityResponse.ActivityContext context = new SplitActivityResponse.ActivityContext();

        if (activity.getGroupId() != null) {
            Optional<Group> group = groupRepository.findById(activity.getGroupId());
            if (group.isPresent()) {
                context.setGroupId(group.get().getId());
                context.setGroupName(group.get().getName());
            }
        }

        if (activity.getSplitExpenseId() != null) {
            Optional<SplitExpense> expense = splitExpenseRepository.findById(activity.getSplitExpenseId());
            if (expense.isPresent()) {
                context.setExpenseId(expense.get().getId());
                context.setExpenseDescription(expense.get().getDescription());
                context.setAmount(expense.get().getTotalAmount());
                context.setCurrency(expense.get().getCurrency());
            }
        }

        // Generate human-readable message
        String message = generateActivityMessage(activity, actor, target, context);

        return new SplitActivityResponse(
            activity.getId(),
            activity.getActivityType(),
            message,
            actor,
            target,
            context,
            activity.getCreatedAt()
        );
    }

    private String generateActivityMessage(SplitActivity activity,
                                         SplitActivityResponse.ActivityActor actor,
                                         SplitActivityResponse.ActivityTarget target,
                                         SplitActivityResponse.ActivityContext context) {

        String actorName = actor != null ? actor.getUsername() : "Someone";
        String targetName = target != null ? target.getUsername() : "someone";

        return switch (activity.getActivityType()) {
            case GROUP_CREATED -> actorName + " created group '" + (context.getGroupName() != null ? context.getGroupName() : "Unknown") + "'";
            case MEMBER_ADDED -> actorName + " added " + targetName + " to " + (context.getGroupName() != null ? context.getGroupName() : "group");
            case MEMBER_REMOVED -> actorName + " removed " + targetName + " from " + (context.getGroupName() != null ? context.getGroupName() : "group");
            case MEMBER_LEFT -> actorName + " left " + (context.getGroupName() != null ? context.getGroupName() : "group");
            case EXPENSE_CREATED -> actorName + " added '" + (context.getExpenseDescription() != null ? context.getExpenseDescription() : "expense") +
                                   "' " + (context.getAmount() != null ? context.getCurrency() + context.getAmount() : "") +
                                   (context.getGroupName() != null ? " in " + context.getGroupName() : "");
            case SETTLEMENT_RECORDED -> actorName + " recorded payment " +
                                      (context.getAmount() != null ? context.getCurrency() + context.getAmount() : "") +
                                      " to " + targetName;
            case PARTICIPANT_SETTLED -> actorName + " marked " + targetName + " as settled";
            case PARTICIPANT_UNSETTLED -> actorName + " marked " + targetName + " as unsettled";
            default -> actorName + " performed " + activity.getActivityType().name().toLowerCase().replace('_', ' ');
        };
    }

    private String toJson(Object object) {
        try {
            return objectMapper.writeValueAsString(object);
        } catch (JsonProcessingException e) {
            return "{}";
        }
    }
}