package com.evbooking.backend.domain.repository.split;

import com.evbooking.backend.domain.model.split.Settlement;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

public interface SettlementRepository {
    Settlement save(Settlement settlement);
    Optional<Settlement> findById(Long id);
    List<Settlement> findByFromUserId(Long fromUserId);
    List<Settlement> findByToUserId(Long toUserId);
    List<Settlement> findByFromUserIdOrToUserId(Long userId1, Long userId2);
    Page<Settlement> findByFromUserIdOrToUserId(Long userId1, Long userId2, Pageable pageable);
    List<Settlement> findBySplitExpenseId(Long splitExpenseId);
    List<Settlement> findBySettlementDateBetween(LocalDateTime startDate, LocalDateTime endDate);
    BigDecimal getTotalSettlementsByFromUserId(Long fromUserId);
    BigDecimal getTotalSettlementsByToUserId(Long toUserId);
    void deleteById(Long id);
}