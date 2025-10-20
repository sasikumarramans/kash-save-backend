package com.evbooking.backend.usecase.service.split;

import com.evbooking.backend.domain.model.User;
import com.evbooking.backend.domain.model.split.Group;
import com.evbooking.backend.domain.model.split.Settlement;
import com.evbooking.backend.domain.repository.UserRepository;
import com.evbooking.backend.domain.repository.split.GroupRepository;
import com.evbooking.backend.domain.repository.split.SettlementRepository;
import com.evbooking.backend.domain.repository.split.SplitExpenseRepository;
import com.evbooking.backend.domain.repository.split.SplitParticipantRepository;
import com.evbooking.backend.infrastructure.mapper.split.GroupMapper;
import com.evbooking.backend.infrastructure.mapper.split.SettlementMapper;
import com.evbooking.backend.presentation.dto.split.SettlementResponse;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
@Transactional
public class SettlementService {

    private final SettlementRepository settlementRepository;
    private final UserRepository userRepository;
    private final GroupRepository groupRepository;
    private final SplitExpenseRepository splitExpenseRepository;
    private final SplitParticipantRepository splitParticipantRepository;
    private final GroupService groupService;
    private final SplitActivityService splitActivityService;
    private final SettlementMapper settlementMapper;
    private final GroupMapper groupMapper;

    public SettlementService(SettlementRepository settlementRepository,
                            UserRepository userRepository,
                            GroupRepository groupRepository,
                            SplitExpenseRepository splitExpenseRepository,
                            SplitParticipantRepository splitParticipantRepository,
                            GroupService groupService,
                            SplitActivityService splitActivityService,
                            SettlementMapper settlementMapper,
                            GroupMapper groupMapper) {
        this.settlementRepository = settlementRepository;
        this.userRepository = userRepository;
        this.groupRepository = groupRepository;
        this.splitExpenseRepository = splitExpenseRepository;
        this.splitParticipantRepository = splitParticipantRepository;
        this.groupService = groupService;
        this.splitActivityService = splitActivityService;
        this.settlementMapper = settlementMapper;
        this.groupMapper = groupMapper;
    }

    /**
     * Create a standalone settlement (person-to-person payment)
     * This is NOT linked to any split expense
     */
    public Settlement createStandaloneSettlement(String fromUsername, String toUsername,
                                                BigDecimal amount, String currency,
                                                Long groupId, String groupType, Long expenseId, String notes,
                                                String recordedByUserId) {

        // Validate users exist
        Optional<User> fromUserOpt = userRepository.findByUsername(fromUsername);
        if (fromUserOpt.isEmpty()) {
            throw new RuntimeException("From user not found with username: " + fromUsername);
        }
        User fromUser = fromUserOpt.get();

        Optional<User> toUserOpt = userRepository.findByUsername(toUsername);
        if (toUserOpt.isEmpty()) {
            throw new RuntimeException("To user not found with username: " + toUsername);
        }
        User toUser = toUserOpt.get();

        // Validate amount
        if (amount.compareTo(BigDecimal.ZERO) <= 0) {
            throw new RuntimeException("Amount must be greater than 0");
        }

        // Validate group membership if groupId provided
        if (groupId != null && groupType != null) {
            if ("group".equals(groupType)) {
                // Real group - validate membership
                if (!groupService.isGroupMember(groupId, fromUser.getId())) {
                    throw new RuntimeException("From user is not a member of this group");
                }
                if (!groupService.isGroupMember(groupId, toUser.getId())) {
                    throw new RuntimeException("To user is not a member of this group");
                }
                // Verify recorded by user is also a member
                if (!groupService.isGroupMember(groupId, recordedByUserId)) {
                    throw new RuntimeException("You are not a member of this group");
                }
            } else if ("expense".equals(groupType)) {
                // Non-group expense - need expenseId to validate
                if (expenseId == null) {
                    throw new RuntimeException("Expense ID is required for non-group expense settlements");
                }

                // Verify expense exists
                var expenseOpt = splitExpenseRepository.findById(expenseId);
                if (expenseOpt.isEmpty()) {
                    throw new RuntimeException("Expense not found");
                }

                // Verify users are participants in the expense
                var fromParticipant = splitParticipantRepository.findBySplitExpenseIdAndUserId(expenseId, fromUser.getId());
                var toParticipant = splitParticipantRepository.findBySplitExpenseIdAndUserId(expenseId, toUser.getId());
                var recordedByParticipant = splitParticipantRepository.findBySplitExpenseIdAndUserId(expenseId, recordedByUserId);

                if (fromParticipant.isEmpty()) {
                    throw new RuntimeException("From user is not a participant in this expense");
                }
                if (toParticipant.isEmpty()) {
                    throw new RuntimeException("To user is not a participant in this expense");
                }
                if (recordedByParticipant.isEmpty()) {
                    throw new RuntimeException("You are not a participant in this expense");
                }
            }
        }

        // Create settlement with NO splitExpenseId (standalone)
        Settlement settlement = new Settlement(
            fromUser.getId(),
            toUser.getId(),
            amount,
            currency != null ? currency : "INR",
            null, // splitExpenseId = null for standalone
            groupId,
            notes,
            recordedByUserId
        );

        var savedEntity = settlementRepository.save(settlementMapper.toEntity(settlement));
        Settlement savedSettlement = settlementMapper.toDomain(savedEntity);

        // Log activity
        splitActivityService.logStandaloneSettlement(
            fromUser.getId(),
            toUser.getId(),
            amount,
            currency != null ? currency : "INR",
            groupId,
            notes
        );

        return savedSettlement;
    }

    /**
     * Get all settlements for a user
     */
    public Page<SettlementResponse> getUserSettlements(String userId, Pageable pageable) {
        var entityPage = settlementRepository.findByUserId(userId, pageable);
        var settlements = entityPage.getContent().stream()
            .map(settlementMapper::toDomain)
            .map(this::convertToResponse)
            .collect(Collectors.toList());

        return new PageImpl<>(settlements, pageable, entityPage.getTotalElements());
    }

    /**
     * Get settlements between two users
     */
    public List<SettlementResponse> getSettlementsBetweenUsers(String userId1, String userId2) {
        var entities = settlementRepository.findBetweenUsers(userId1, userId2);
        return entities.stream()
            .map(settlementMapper::toDomain)
            .map(this::convertToResponse)
            .collect(Collectors.toList());
    }

    /**
     * Get settlements for a group or non-group expenses
     * For non-group expenses: if groupId=0, returns ALL non-group settlements. If expenseId provided, filters by that expense.
     */
    public Page<SettlementResponse> getGroupSettlements(Long groupId, String groupType, String userId, Pageable pageable) {
        List<SettlementResponse> settlements;

        if (groupType != null && "expense".equals(groupType) && groupId != null && groupId == 0) {
            // Virtual "Non-Group Expenses" - return all non-group expense settlements
            // Get all non-group expenses for this user
            var nonGroupExpenses = splitExpenseRepository.findByGroupIdIsNull(userId, PageRequest.of(0, 1000));

            // Get settlements for all these expenses (settlements with groupId = null)
            var allSettlements = settlementRepository.findByGroupId(null);

            // Filter to only settlements where user is a participant in the expense
            settlements = allSettlements.stream()
                .map(settlementMapper::toDomain)
                .filter(settlement ->
                    settlement.getFromUserId().equals(userId) ||
                    settlement.getToUserId().equals(userId))
                .map(this::convertToResponse)
                .collect(Collectors.toList());

        } else if (groupType != null && "group".equals(groupType)) {
            // Real group - verify user is group member
            if (!groupService.isGroupMember(groupId, userId)) {
                throw new RuntimeException("You are not a member of this group");
            }

            var entities = settlementRepository.findByGroupId(groupId);
            settlements = entities.stream()
                .map(settlementMapper::toDomain)
                .map(this::convertToResponse)
                .collect(Collectors.toList());
        } else {
            // Default behavior - return settlements for the given groupId
            var entities = settlementRepository.findByGroupId(groupId);
            settlements = entities.stream()
                .map(settlementMapper::toDomain)
                .map(this::convertToResponse)
                .collect(Collectors.toList());
        }

        // For pagination, use PageImpl
        int start = (int) pageable.getOffset();
        int end = Math.min((start + pageable.getPageSize()), settlements.size());
        List<SettlementResponse> pageContent = settlements.subList(start, end);

        return new PageImpl<>(pageContent, pageable, settlements.size());
    }

    private SettlementResponse convertToResponse(Settlement settlement) {
        // Get user details
        String fromUsername = null;
        Optional<User> fromUser = userRepository.findById(settlement.getFromUserId());
        if (fromUser.isPresent()) {
            fromUsername = fromUser.get().getUsername();
        }

        String toUsername = null;
        Optional<User> toUser = userRepository.findById(settlement.getToUserId());
        if (toUser.isPresent()) {
            toUsername = toUser.get().getUsername();
        }

        String recordedByUsername = null;
        Optional<User> recordedBy = userRepository.findById(settlement.getRecordedByUserId());
        if (recordedBy.isPresent()) {
            recordedByUsername = recordedBy.get().getUsername();
        }

        // Get group name if applicable
        String groupName = null;
        if (settlement.getGroupId() != null) {
            var groupEntityOpt = groupRepository.findById(settlement.getGroupId());
            if (groupEntityOpt.isPresent()) {
                Group group = groupMapper.toDomain(groupEntityOpt.get());
                groupName = group.getName();
            }
        }

        return new SettlementResponse(
            settlement.getId(),
            settlement.getFromUserId(),
            fromUsername,
            settlement.getToUserId(),
            toUsername,
            settlement.getAmount(),
            settlement.getCurrency(),
            settlement.getSplitExpenseId(),
            settlement.getGroupId(),
            groupName,
            settlement.getNotes(),
            settlement.getSettlementDate(),
            settlement.getRecordedByUserId(),
            recordedByUsername
        );
    }
}
