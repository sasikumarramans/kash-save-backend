package com.evbooking.backend.domain.repository.split;

import com.evbooking.backend.domain.model.split.SplitExpense;
import com.evbooking.backend.domain.model.split.SplitType;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

public interface SplitExpenseRepository {
    SplitExpense save(SplitExpense splitExpense);
    Optional<SplitExpense> findById(Long id);
    List<SplitExpense> findByCreatedByUserId(Long userId);
    Page<SplitExpense> findByCreatedByUserId(Long userId, Pageable pageable);
    List<SplitExpense> findByGroupId(Long groupId);
    Page<SplitExpense> findByGroupId(Long groupId, Pageable pageable);
    List<SplitExpense> findByPaidByUserId(Long userId);
    List<SplitExpense> findByGroupIdIsNull();
    Page<SplitExpense> findByGroupIdIsNull(Pageable pageable);
    List<SplitExpense> findBySplitType(SplitType splitType);
    List<SplitExpense> findByCreatedAtBetween(LocalDateTime startDate, LocalDateTime endDate);
    List<SplitExpense> findExpensesByParticipantUserId(Long userId);
    Page<SplitExpense> findExpensesByParticipantUserId(Long userId, Pageable pageable);
    Page<SplitExpense> findGroupAndRelatedIndividualExpenses(Long groupId, List<Long> groupMemberIds, Long currentUserId, Pageable pageable);
    List<SplitExpense> findIndividualExpensesBetweenUsers(Long userId1, Long userId2);
    void deleteById(Long id);
}