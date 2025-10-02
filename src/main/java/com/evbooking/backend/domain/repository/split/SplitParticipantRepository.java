package com.evbooking.backend.domain.repository.split;

import com.evbooking.backend.infrastructure.entity.split.SplitParticipantEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface SplitParticipantRepository extends JpaRepository<SplitParticipantEntity, Long> {

    List<SplitParticipantEntity> findBySplitExpenseId(Long splitExpenseId);

    List<SplitParticipantEntity> findByUserId(Long userId);

    Optional<SplitParticipantEntity> findBySplitExpenseIdAndUserId(Long splitExpenseId, Long userId);

    List<SplitParticipantEntity> findByUserIdAndIsSettledFalse(Long userId);

    @Query("SELECT sp FROM SplitParticipantEntity sp JOIN SplitExpenseEntity se ON sp.splitExpenseId = se.id WHERE se.groupId = :groupId")
    List<SplitParticipantEntity> findByGroupId(@Param("groupId") Long groupId);

    void deleteBySplitExpenseId(Long splitExpenseId);
}