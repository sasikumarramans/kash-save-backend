package com.evbooking.backend.domain.repository.split;

import com.evbooking.backend.infrastructure.entity.split.SettlementEntity;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface SettlementRepository extends JpaRepository<SettlementEntity, Long> {

    @Query("SELECT s FROM SettlementEntity s WHERE s.fromUserId = :userId OR s.toUserId = :userId")
    Page<SettlementEntity> findByUserId(@Param("userId") Long userId, Pageable pageable);

    List<SettlementEntity> findByFromUserId(Long fromUserId);

    List<SettlementEntity> findByToUserId(Long toUserId);

    @Query("SELECT s FROM SettlementEntity s WHERE (s.fromUserId = :userId1 AND s.toUserId = :userId2) OR (s.fromUserId = :userId2 AND s.toUserId = :userId1)")
    List<SettlementEntity> findBetweenUsers(@Param("userId1") Long userId1, @Param("userId2") Long userId2);

    List<SettlementEntity> findBySplitExpenseId(Long splitExpenseId);

    List<SettlementEntity> findByGroupId(Long groupId);
}