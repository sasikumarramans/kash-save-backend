package com.evbooking.backend.domain.repository.split;

import com.evbooking.backend.domain.model.split.SplitParticipant;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

public interface SplitParticipantRepository {
    SplitParticipant save(SplitParticipant splitParticipant);
    Optional<SplitParticipant> findById(Long id);
    Optional<SplitParticipant> findBySplitExpenseIdAndUserId(Long splitExpenseId, Long userId);
    List<SplitParticipant> findBySplitExpenseId(Long splitExpenseId);
    List<SplitParticipant> findByUserId(Long userId);
    List<SplitParticipant> findByUserIdAndIsSettled(Long userId, boolean isSettled);
    void deleteBySplitExpenseIdAndUserId(Long splitExpenseId, Long userId);
    void deleteBySplitExpenseId(Long splitExpenseId);
    BigDecimal getTotalAmountOwedByUserId(Long userId);
    BigDecimal getTotalAmountOwedToUserId(Long userId);
    long countUnsettledByUserId(Long userId);
}