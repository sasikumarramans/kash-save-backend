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
    Page<SplitExpense> findByUserId(Long userId, Pageable pageable);
    Page<SplitExpense> findIndividualExpensesByUserId(Long userId, Pageable pageable);
    Page<SplitExpense> findByGroupId(Long groupId, Pageable pageable);
    Optional<SplitExpense> findByIdAndUserId(Long expenseId, Long userId);
    Page<SplitExpense> findByGroupIdAndUserId(Long groupId, Long userId, Pageable pageable);
    List<SplitExpense> findByCreatedByUserId(Long userId);
    List<SplitExpense> findByPaidByUserId(Long userId);
    void deleteById(Long id);
}