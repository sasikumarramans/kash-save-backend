package com.evbooking.backend.domain.repository.split;

import com.evbooking.backend.infrastructure.entity.split.SplitExpenseEntity;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface SplitExpenseRepository extends JpaRepository<SplitExpenseEntity, Long> {

    @Query("SELECT se FROM SplitExpenseEntity se JOIN SplitParticipantEntity sp ON se.id = sp.splitExpenseId WHERE sp.userId = :userId")
    List<SplitExpenseEntity> findExpensesByParticipantUserId(@Param("userId") Long userId);

    @Query("SELECT se FROM SplitExpenseEntity se JOIN SplitParticipantEntity sp ON se.id = sp.splitExpenseId WHERE sp.userId = :userId")
    Page<SplitExpenseEntity> findExpensesByParticipantUserIdPaged(@Param("userId") Long userId, Pageable pageable);

    @Query("SELECT se FROM SplitExpenseEntity se WHERE se.groupId IS NULL AND (se.createdByUserId = :userId OR se.paidByUserId = :userId OR se.id IN (SELECT sp.splitExpenseId FROM SplitParticipantEntity sp WHERE sp.userId = :userId))")
    Page<SplitExpenseEntity> findByGroupIdIsNull(@Param("userId") Long userId, Pageable pageable);

    @Query("SELECT se FROM SplitExpenseEntity se WHERE se.groupId = :groupId AND (se.createdByUserId = :userId OR se.paidByUserId = :userId OR se.id IN (SELECT sp.splitExpenseId FROM SplitParticipantEntity sp WHERE sp.userId = :userId))")
    Page<SplitExpenseEntity> findByGroupIdAndUserId(@Param("groupId") Long groupId, @Param("userId") Long userId, Pageable pageable);

    @Query("SELECT se FROM SplitExpenseEntity se WHERE se.id = :expenseId AND (se.createdByUserId = :userId OR se.paidByUserId = :userId OR se.id IN (SELECT sp.splitExpenseId FROM SplitParticipantEntity sp WHERE sp.userId = :userId))")
    Optional<SplitExpenseEntity> findByIdAndUserId(@Param("expenseId") Long expenseId, @Param("userId") Long userId);

    @Query("SELECT se FROM SplitExpenseEntity se WHERE " +
           "(se.groupId = :groupId OR " +
           "(se.groupId IS NULL AND se.createdByUserId IN :memberUserIds AND se.paidByUserId IN :memberUserIds)) " +
           "AND (se.createdByUserId = :currentUserId OR se.paidByUserId = :currentUserId OR se.id IN " +
           "(SELECT sp.splitExpenseId FROM SplitParticipantEntity sp WHERE sp.userId = :currentUserId))")
    Page<SplitExpenseEntity> findGroupAndRelatedIndividualExpenses(@Param("groupId") Long groupId,
                                                                   @Param("memberUserIds") List<Long> memberUserIds,
                                                                   @Param("currentUserId") Long currentUserId,
                                                                   Pageable pageable);

    Page<SplitExpenseEntity> findByGroupId(Long groupId, Pageable pageable);

    List<SplitExpenseEntity> findByCreatedByUserId(Long userId);

    List<SplitExpenseEntity> findByPaidByUserId(Long userId);
}