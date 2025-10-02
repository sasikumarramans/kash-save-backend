package com.evbooking.backend.usecase.service.split;

import com.evbooking.backend.domain.model.User;
import com.evbooking.backend.domain.model.split.*;
import com.evbooking.backend.domain.repository.UserRepository;
import com.evbooking.backend.domain.repository.split.*;
import com.evbooking.backend.domain.service.split.SplitCalculationService;
import com.evbooking.backend.infrastructure.mapper.split.*;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.*;
import java.util.stream.Collectors;

@Service
@Transactional
public class SplitExpenseService {

    private final SplitExpenseRepository splitExpenseRepository;
    private final SplitParticipantRepository splitParticipantRepository;
    private final GroupMemberRepository groupMemberRepository;
    private final UserRepository userRepository;
    private final GroupService groupService;
    private final SplitCalculationService splitCalculationService;
    private final SplitActivityService splitActivityService;
    private final SplitExpenseMapper splitExpenseMapper;
    private final SplitParticipantMapper splitParticipantMapper;
    private final GroupMemberMapper groupMemberMapper;

    public SplitExpenseService(SplitExpenseRepository splitExpenseRepository,
                              SplitParticipantRepository splitParticipantRepository,
                              GroupMemberRepository groupMemberRepository,
                              UserRepository userRepository,
                              GroupService groupService,
                              SplitCalculationService splitCalculationService,
                              SplitActivityService splitActivityService,
                              SplitExpenseMapper splitExpenseMapper,
                              SplitParticipantMapper splitParticipantMapper,
                              GroupMemberMapper groupMemberMapper) {
        this.splitExpenseRepository = splitExpenseRepository;
        this.splitParticipantRepository = splitParticipantRepository;
        this.groupMemberRepository = groupMemberRepository;
        this.userRepository = userRepository;
        this.groupService = groupService;
        this.splitCalculationService = splitCalculationService;
        this.splitActivityService = splitActivityService;
        this.splitExpenseMapper = splitExpenseMapper;
        this.splitParticipantMapper = splitParticipantMapper;
        this.groupMemberMapper = groupMemberMapper;
    }

    public SplitExpense createSplitExpense(String description, BigDecimal totalAmount, String currency,
                                         String paidByUsername, Long groupId, SplitType splitType,
                                         Map<String, BigDecimal> participantSplitValues, Long createdByUserId) {

        // Find paid by user
        Optional<User> paidByUserOpt = userRepository.findByUsername(paidByUsername);
        if (paidByUserOpt.isEmpty()) {
            throw new RuntimeException("User not found with username: " + paidByUsername);
        }
        User paidByUser = paidByUserOpt.get();

        // Validate group membership if group split
        if (groupId != null) {
            if (!groupService.isGroupMember(groupId, createdByUserId)) {
                throw new RuntimeException("You are not a member of this group");
            }
            if (!groupService.isGroupMember(groupId, paidByUser.getId())) {
                throw new RuntimeException("Paid by user is not a member of this group");
            }
        }

        // Convert usernames to user IDs and validate
        Map<Long, BigDecimal> participantSplitMap = new HashMap<>();
        for (Map.Entry<String, BigDecimal> entry : participantSplitValues.entrySet()) {
            Optional<User> userOpt = userRepository.findByUsername(entry.getKey());
            if (userOpt.isEmpty()) {
                throw new RuntimeException("User not found with username: " + entry.getKey());
            }

            User user = userOpt.get();

            // Validate group membership for group splits
            if (groupId != null && !groupService.isGroupMember(groupId, user.getId())) {
                throw new RuntimeException("User " + entry.getKey() + " is not a member of this group");
            }

            participantSplitMap.put(user.getId(), entry.getValue());
        }

        // Validate split calculation
        splitCalculationService.validateSplitRequest(totalAmount, splitType, participantSplitMap);

        // Calculate individual amounts
        Map<Long, BigDecimal> calculatedAmounts = splitCalculationService.calculateSplitAmounts(
            totalAmount, splitType, participantSplitMap);

        // Create split expense
        SplitExpense splitExpense = new SplitExpense(description, totalAmount, currency,
            paidByUser.getId(), groupId, splitType, createdByUserId);
        var savedEntity = splitExpenseRepository.save(splitExpenseMapper.toEntity(splitExpense));
        splitExpense = splitExpenseMapper.toDomain(savedEntity);

        // Create split participants
        for (Map.Entry<Long, BigDecimal> entry : calculatedAmounts.entrySet()) {
            Long userId = entry.getKey();
            BigDecimal amountOwed = entry.getValue();
            BigDecimal splitValue = participantSplitMap.get(userId);

            SplitParticipant participant = new SplitParticipant(splitExpense.getId(), userId, amountOwed, splitValue);

            // If the participant is the one who paid, mark as settled
            if (userId.equals(paidByUser.getId())) {
                participant.setSettled(true);
            }

            splitParticipantRepository.save(splitParticipantMapper.toEntity(participant));
        }

        // Log expense creation activity
        splitActivityService.logExpenseCreated(splitExpense.getId(), createdByUserId);

        return splitExpense;
    }

    public Optional<SplitExpense> getSplitExpenseById(Long expenseId, Long userId) {
        var expenseEntityOpt = splitExpenseRepository.findById(expenseId);
        if (expenseEntityOpt.isEmpty()) {
            return Optional.empty();
        }

        SplitExpense expense = splitExpenseMapper.toDomain(expenseEntityOpt.get());

        // Check if user is involved in this expense
        if (expense.getGroupId() != null) {
            // Group expense - check group membership
            if (!groupService.isGroupMember(expense.getGroupId(), userId)) {
                return Optional.empty();
            }
        } else {
            // Individual expense - check if user is a participant or creator
            var participantEntities = splitParticipantRepository.findBySplitExpenseId(expenseId);
            List<SplitParticipant> participants = participantEntities.stream()
                .map(splitParticipantMapper::toDomain)
                .collect(Collectors.toList());
            boolean isInvolved = participants.stream().anyMatch(p -> p.getUserId().equals(userId)) ||
                               expense.getCreatedByUserId().equals(userId) ||
                               expense.getPaidByUserId().equals(userId);
            if (!isInvolved) {
                return Optional.empty();
            }
        }

        return Optional.of(expense);
    }

    public Page<SplitExpense> getUserSplitExpenses(Long userId, Pageable pageable) {
        var entityPage = splitExpenseRepository.findExpensesByParticipantUserIdPaged(userId, pageable);
        var domainList = entityPage.getContent().stream()
            .map(splitExpenseMapper::toDomain)
            .collect(Collectors.toList());
        return new PageImpl<>(domainList, pageable, entityPage.getTotalElements());
    }

    public Page<SplitExpense> getGroupSplitExpenses(Long groupId, Long userId, Pageable pageable) {
        // Verify group membership
        if (!groupService.isGroupMember(groupId, userId)) {
            throw new RuntimeException("You are not a member of this group");
        }

        // Get group members to include individual expenses between them
        var groupMemberEntities = groupMemberRepository.findByGroupId(groupId);
        List<Long> memberUserIds = groupMemberEntities.stream()
            .map(entity -> entity.getUserId())
            .collect(Collectors.toList());

        // Return both group expenses and individual expenses involving group members
        var entityPage = splitExpenseRepository.findGroupAndRelatedIndividualExpenses(groupId, memberUserIds, userId, pageable);
        var domainList = entityPage.getContent().stream()
            .map(splitExpenseMapper::toDomain)
            .collect(Collectors.toList());
        return new PageImpl<>(domainList, pageable, entityPage.getTotalElements());
    }

    public Page<SplitExpense> getIndividualSplitExpenses(Long userId, Pageable pageable) {
        // Get all individual (non-group) expenses where user is involved
        var entityPage = splitExpenseRepository.findByGroupIdIsNull(userId, pageable);
        var domainList = entityPage.getContent().stream()
            .map(splitExpenseMapper::toDomain)
            .collect(Collectors.toList());
        return new PageImpl<>(domainList, pageable, entityPage.getTotalElements());
    }

    public List<SplitParticipant> getSplitParticipants(Long expenseId, Long userId) {
        // Verify user can access this expense
        if (getSplitExpenseById(expenseId, userId).isEmpty()) {
            throw new RuntimeException("Split expense not found or access denied");
        }

        var participantEntities = splitParticipantRepository.findBySplitExpenseId(expenseId);
        return participantEntities.stream()
            .map(splitParticipantMapper::toDomain)
            .collect(Collectors.toList());
    }

    public void deleteSplitExpense(Long expenseId, Long userId) {
        Optional<SplitExpense> expenseOpt = getSplitExpenseById(expenseId, userId);
        if (expenseOpt.isEmpty()) {
            throw new RuntimeException("Split expense not found or access denied");
        }

        SplitExpense expense = expenseOpt.get();

        // Only creator or group admin can delete
        boolean canDelete = expense.getCreatedByUserId().equals(userId);
        if (expense.getGroupId() != null) {
            canDelete = canDelete || groupService.isGroupAdmin(expense.getGroupId(), userId);
        }

        if (!canDelete) {
            throw new RuntimeException("Only the creator or group admin can delete this expense");
        }

        splitExpenseRepository.deleteById(expenseId);
    }

    public SplitParticipant updateParticipantSettlement(Long expenseId, Long participantUserId,
                                                       boolean isSettled, Long updatedByUserId) {
        // Verify access to expense
        if (getSplitExpenseById(expenseId, updatedByUserId).isEmpty()) {
            throw new RuntimeException("Split expense not found or access denied");
        }

        var participantEntityOpt = splitParticipantRepository
            .findBySplitExpenseIdAndUserId(expenseId, participantUserId);

        if (participantEntityOpt.isEmpty()) {
            throw new RuntimeException("Participant not found in this expense");
        }

        var participantEntity = participantEntityOpt.get();
        SplitParticipant participant = splitParticipantMapper.toDomain(participantEntity);
        participant.setSettled(isSettled);

        var savedEntity = splitParticipantRepository.save(splitParticipantMapper.toEntity(participant));
        SplitParticipant savedParticipant = splitParticipantMapper.toDomain(savedEntity);

        // Log participant settlement activity
        splitActivityService.logParticipantSettled(expenseId, participantUserId, updatedByUserId, isSettled);

        return savedParticipant;
    }
}