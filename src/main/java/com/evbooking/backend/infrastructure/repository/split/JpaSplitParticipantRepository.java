package com.evbooking.backend.infrastructure.repository.split;

import com.evbooking.backend.infrastructure.entity.split.SplitParticipantEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface JpaSplitParticipantRepository extends JpaRepository<SplitParticipantEntity, Long> {

    List<SplitParticipantEntity> findBySplitExpenseId(Long splitExpenseId);

    List<SplitParticipantEntity> findByUserId(String userId);

    @Query("SELECT sp FROM SplitParticipantEntity sp WHERE sp.splitExpenseId = :expenseId AND sp.userId = :userId")
    Optional<SplitParticipantEntity> findBySplitExpenseIdAndUserId(@Param("expenseId") String userId);

    List<SplitParticipantEntity> findByUserIdAndIsSettledFalse(String userId);

    @Query("SELECT sp FROM SplitParticipantEntity sp WHERE sp.splitExpenseId IN (SELECT se.id FROM SplitExpenseEntity se WHERE se.groupId = :groupId)")
    List<SplitParticipantEntity> findByGroupId(@Param("groupId") Long groupId);

    void deleteBySplitExpenseId(Long splitExpenseId);
}