package com.evbooking.backend.domain.repository.split;

import com.evbooking.backend.infrastructure.entity.split.SplitActivityEntity;
import com.evbooking.backend.domain.model.split.SplitActivityType;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface SplitActivityRepository extends JpaRepository<SplitActivityEntity, Long> {

    // Get activities for a user (activities that affect them)
    @Query("SELECT sa FROM SplitActivityEntity sa WHERE sa.userId = :userId OR sa.relatedUserId = :userId ORDER BY sa.createdAt DESC")
    Page<SplitActivityEntity> findActivitiesForUser(@Param("userId") Long userId, Pageable pageable);

    @Query("SELECT sa FROM SplitActivityEntity sa WHERE sa.userId = :userId OR sa.relatedUserId = :userId ORDER BY sa.createdAt DESC")
    List<SplitActivityEntity> findActivitiesForUser(@Param("userId") Long userId);

    // Filter by activity type
    @Query("SELECT sa FROM SplitActivityEntity sa WHERE sa.activityType = :activityType AND (sa.userId = :userId OR sa.relatedUserId = :userId) ORDER BY sa.createdAt DESC")
    Page<SplitActivityEntity> findByActivityTypeAndUser(@Param("activityType") SplitActivityType activityType, @Param("userId") Long userId, Pageable pageable);

    // Filter by time range
    @Query("SELECT sa FROM SplitActivityEntity sa WHERE sa.createdAt BETWEEN :startDate AND :endDate AND (sa.userId = :userId OR sa.relatedUserId = :userId) ORDER BY sa.createdAt DESC")
    Page<SplitActivityEntity> findByCreatedAtBetweenAndUser(@Param("startDate") LocalDateTime startDate, @Param("endDate") LocalDateTime endDate, @Param("userId") Long userId, Pageable pageable);

    // Group-specific activities
    @Query("SELECT sa FROM SplitActivityEntity sa WHERE sa.groupId = :groupId AND (sa.userId = :userId OR sa.relatedUserId = :userId) ORDER BY sa.createdAt DESC")
    Page<SplitActivityEntity> findByGroupIdAndUser(@Param("groupId") Long groupId, @Param("userId") Long userId, Pageable pageable);

    List<SplitActivityEntity> findByGroupId(Long groupId);

    // Expense-specific activities
    @Query("SELECT sa FROM SplitActivityEntity sa WHERE sa.splitExpenseId = :splitExpenseId AND (sa.userId = :userId OR sa.relatedUserId = :userId) ORDER BY sa.createdAt DESC")
    Page<SplitActivityEntity> findBySplitExpenseIdAndUser(@Param("splitExpenseId") Long splitExpenseId, @Param("userId") Long userId, Pageable pageable);

    List<SplitActivityEntity> findBySplitExpenseId(Long splitExpenseId);

    // Friend-related activities
    @Query("SELECT sa FROM SplitActivityEntity sa WHERE ((sa.userId = :userId AND sa.relatedUserId = :friendId) OR (sa.userId = :friendId AND sa.relatedUserId = :userId)) AND sa.groupId IS NULL ORDER BY sa.createdAt DESC")
    Page<SplitActivityEntity> findFriendActivitiesForUser(@Param("userId") Long userId, @Param("friendId") Long friendId, Pageable pageable);

    // Recent activity - use Pageable to limit results
    @Query("SELECT sa FROM SplitActivityEntity sa WHERE sa.userId = :userId OR sa.relatedUserId = :userId ORDER BY sa.createdAt DESC")
    List<SplitActivityEntity> findRecentActivitiesForUser(@Param("userId") Long userId, Pageable pageable);

    void deleteByGroupId(Long groupId);
    void deleteBySplitExpenseId(Long splitExpenseId);
}