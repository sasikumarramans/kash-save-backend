package com.evbooking.backend.infrastructure.jpa.split;

import com.evbooking.backend.domain.model.split.SplitActivityType;
import com.evbooking.backend.infrastructure.entity.split.SplitActivityEntity;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDateTime;
import java.util.List;

public interface SplitActivityJpaRepository extends JpaRepository<SplitActivityEntity, Long> {

    // Find activities where user is involved (either as actor or target)
    @Query("""
        SELECT sa FROM SplitActivityEntity sa
        WHERE sa.userId = :userId
           OR sa.relatedUserId = :userId
           OR (sa.groupId IN (
               SELECT gm.groupId FROM GroupMemberEntity gm WHERE gm.userId = :userId
           ))
        ORDER BY sa.createdAt DESC
    """)
    Page<SplitActivityEntity> findActivitiesForUser(@Param("userId") Long userId, Pageable pageable);

    @Query("""
        SELECT sa FROM SplitActivityEntity sa
        WHERE sa.userId = :userId
           OR sa.relatedUserId = :userId
           OR (sa.groupId IN (
               SELECT gm.groupId FROM GroupMemberEntity gm WHERE gm.userId = :userId
           ))
        ORDER BY sa.createdAt DESC
    """)
    List<SplitActivityEntity> findActivitiesForUser(@Param("userId") Long userId);

    // Find activities by type for user
    @Query("""
        SELECT sa FROM SplitActivityEntity sa
        WHERE sa.activityType = :activityType
          AND (sa.userId = :userId
               OR sa.relatedUserId = :userId
               OR (sa.groupId IN (
                   SELECT gm.groupId FROM GroupMemberEntity gm WHERE gm.userId = :userId
               )))
        ORDER BY sa.createdAt DESC
    """)
    Page<SplitActivityEntity> findByActivityTypeAndUser(@Param("activityType") SplitActivityType activityType,
                                                       @Param("userId") Long userId,
                                                       Pageable pageable);

    // Find activities within date range for user
    @Query("""
        SELECT sa FROM SplitActivityEntity sa
        WHERE sa.createdAt BETWEEN :startDate AND :endDate
          AND (sa.userId = :userId
               OR sa.relatedUserId = :userId
               OR (sa.groupId IN (
                   SELECT gm.groupId FROM GroupMemberEntity gm WHERE gm.userId = :userId
               )))
        ORDER BY sa.createdAt DESC
    """)
    Page<SplitActivityEntity> findByCreatedAtBetweenAndUser(@Param("startDate") LocalDateTime startDate,
                                                           @Param("endDate") LocalDateTime endDate,
                                                           @Param("userId") Long userId,
                                                           Pageable pageable);

    // Find group activities for user
    @Query("""
        SELECT sa FROM SplitActivityEntity sa
        WHERE sa.groupId = :groupId
          AND sa.groupId IN (
              SELECT gm.groupId FROM GroupMemberEntity gm WHERE gm.userId = :userId
          )
        ORDER BY sa.createdAt DESC
    """)
    Page<SplitActivityEntity> findByGroupIdAndUser(@Param("groupId") Long groupId,
                                                  @Param("userId") Long userId,
                                                  Pageable pageable);

    List<SplitActivityEntity> findByGroupIdOrderByCreatedAtDesc(Long groupId);

    // Find expense activities for user
    @Query("""
        SELECT sa FROM SplitActivityEntity sa
        WHERE sa.splitExpenseId = :splitExpenseId
          AND (sa.userId = :userId OR sa.relatedUserId = :userId)
        ORDER BY sa.createdAt DESC
    """)
    Page<SplitActivityEntity> findBySplitExpenseIdAndUser(@Param("splitExpenseId") Long splitExpenseId,
                                                         @Param("userId") Long userId,
                                                         Pageable pageable);

    List<SplitActivityEntity> findBySplitExpenseIdOrderByCreatedAtDesc(Long splitExpenseId);

    // Find friend activities between two users
    @Query("""
        SELECT sa FROM SplitActivityEntity sa
        WHERE ((sa.userId = :userId AND sa.relatedUserId = :friendId)
               OR (sa.userId = :friendId AND sa.relatedUserId = :userId))
           OR (sa.splitExpenseId IN (
               SELECT sp.splitExpenseId FROM SplitParticipantEntity sp
               WHERE sp.userId IN (:userId, :friendId)
               GROUP BY sp.splitExpenseId
               HAVING COUNT(DISTINCT sp.userId) = 2
               AND :userId IN (SELECT sp2.userId FROM SplitParticipantEntity sp2 WHERE sp2.splitExpenseId = sp.splitExpenseId)
               AND :friendId IN (SELECT sp3.userId FROM SplitParticipantEntity sp3 WHERE sp3.splitExpenseId = sp.splitExpenseId)
           ))
        ORDER BY sa.createdAt DESC
    """)
    Page<SplitActivityEntity> findFriendActivitiesForUser(@Param("userId") Long userId,
                                                         @Param("friendId") Long friendId,
                                                         Pageable pageable);

    // Get recent activities with limit
    @Query(value = """
        SELECT sa.* FROM split_activities sa
        WHERE sa.user_id = :userId
           OR sa.related_user_id = :userId
           OR sa.group_id IN (
               SELECT gm.group_id FROM group_members gm WHERE gm.user_id = :userId
           )
        ORDER BY sa.created_at DESC
        LIMIT :limit
    """, nativeQuery = true)
    List<SplitActivityEntity> findRecentActivitiesForUser(@Param("userId") Long userId, @Param("limit") int limit);

    // Delete methods
    void deleteByGroupId(Long groupId);
    void deleteBySplitExpenseId(Long splitExpenseId);
}