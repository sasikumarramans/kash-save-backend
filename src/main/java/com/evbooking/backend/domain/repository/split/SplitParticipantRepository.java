package com.evbooking.backend.domain.repository.split;

import com.evbooking.backend.domain.model.split.SplitParticipant;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

public interface SplitParticipantRepository {
    SplitParticipant save(SplitParticipant splitParticipant);
    List<SplitParticipant> findBySplitExpenseId(Long splitExpenseId);
    List<SplitParticipant> findByUserId(Long userId);
    Optional<SplitParticipant> findBySplitExpenseIdAndUserId(Long expenseId, Long userId);
    List<SplitParticipant> findByUserIdAndIsSettledFalse(Long userId);
    List<SplitParticipant> findByGroupId(Long groupId);
    void deleteBySplitExpenseId(Long splitExpenseId);
}