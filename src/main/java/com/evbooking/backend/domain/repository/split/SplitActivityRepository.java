package com.evbooking.backend.domain.repository.split;

import com.evbooking.backend.domain.model.split.SplitActivity;
import com.evbooking.backend.domain.model.split.SplitActivityType;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

public interface SplitActivityRepository {
    SplitActivity save(SplitActivity activity);
    Optional<SplitActivity> findById(Long id);

    // Get activities for a user (activities that affect them)
    Page<SplitActivity> findActivitiesForUser(Long userId, Pageable pageable);
    List<SplitActivity> findActivitiesForUser(Long userId);

    // Filter by activity type
    Page<SplitActivity> findByActivityTypeAndUser(SplitActivityType activityType, Long userId, Pageable pageable);

    // Filter by time range
    Page<SplitActivity> findByCreatedAtBetweenAndUser(LocalDateTime startDate, LocalDateTime endDate, Long userId, Pageable pageable);

    // Group-specific activities
    Page<SplitActivity> findByGroupIdAndUser(Long groupId, Long userId, Pageable pageable);
    List<SplitActivity> findByGroupId(Long groupId);

    // Expense-specific activities
    Page<SplitActivity> findBySplitExpenseIdAndUser(Long splitExpenseId, Long userId, Pageable pageable);
    List<SplitActivity> findBySplitExpenseId(Long splitExpenseId);

    // Friend-related activities
    Page<SplitActivity> findFriendActivitiesForUser(Long userId, Long friendId, Pageable pageable);

    // Recent activity
    List<SplitActivity> findRecentActivitiesForUser(Long userId, int limit);

    void deleteById(Long id);
    void deleteByGroupId(Long groupId);
    void deleteBySplitExpenseId(Long splitExpenseId);
}