package com.evbooking.backend.presentation.controller.split;

import com.evbooking.backend.presentation.dto.ApiResponse;
import com.evbooking.backend.presentation.dto.split.SplitActivityResponse;
import com.evbooking.backend.usecase.service.split.SplitActivityService;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/splits/activities")
public class SplitActivityController {

    private final SplitActivityService splitActivityService;

    public SplitActivityController(SplitActivityService splitActivityService) {
        this.splitActivityService = splitActivityService;
    }

    /**
     * Get user's activity feed with filtering
     * @param type Filter: all, groups, expenses, payments, friends
     */
    @GetMapping
    public ResponseEntity<ApiResponse<Page<SplitActivityResponse>>> getActivities(
            @RequestParam(defaultValue = "all") String type,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size,
            @RequestParam(defaultValue = "createdAt") String sortBy,
            @RequestParam(defaultValue = "desc") String sortDir,
            HttpServletRequest httpRequest) {
        try {
            Long userId = (Long) httpRequest.getAttribute("userId");
            if (userId == null) {
                return ResponseEntity.badRequest()
                    .body(ApiResponse.error("User not authenticated"));
            }

            Sort sort = Sort.by(sortDir.equalsIgnoreCase("desc") ?
                Sort.Direction.DESC : Sort.Direction.ASC, sortBy);
            Pageable pageable = PageRequest.of(page, size, sort);

            Page<SplitActivityResponse> activities = splitActivityService.getActivitiesForUser(userId, type, pageable);
            return ResponseEntity.ok(ApiResponse.success(activities));

        } catch (Exception e) {
            return ResponseEntity.badRequest()
                .body(ApiResponse.error(e.getMessage()));
        }
    }

    /**
     * Get recent activities (for dashboard/home screen)
     */
    @GetMapping("/recent")
    public ResponseEntity<ApiResponse<List<SplitActivityResponse>>> getRecentActivities(
            @RequestParam(defaultValue = "10") int limit,
            HttpServletRequest httpRequest) {
        try {
            Long userId = (Long) httpRequest.getAttribute("userId");
            if (userId == null) {
                return ResponseEntity.badRequest()
                    .body(ApiResponse.error("User not authenticated"));
            }

            List<SplitActivityResponse> activities = splitActivityService.getRecentActivities(userId, limit);
            return ResponseEntity.ok(ApiResponse.success(activities));

        } catch (Exception e) {
            return ResponseEntity.badRequest()
                .body(ApiResponse.error(e.getMessage()));
        }
    }

    /**
     * Get activities for a specific group
     */
    @GetMapping("/groups/{groupId}")
    public ResponseEntity<ApiResponse<Page<SplitActivityResponse>>> getGroupActivities(
            @PathVariable Long groupId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size,
            @RequestParam(defaultValue = "createdAt") String sortBy,
            @RequestParam(defaultValue = "desc") String sortDir,
            HttpServletRequest httpRequest) {
        try {
            Long userId = (Long) httpRequest.getAttribute("userId");
            if (userId == null) {
                return ResponseEntity.badRequest()
                    .body(ApiResponse.error("User not authenticated"));
            }

            Sort sort = Sort.by(sortDir.equalsIgnoreCase("desc") ?
                Sort.Direction.DESC : Sort.Direction.ASC, sortBy);
            Pageable pageable = PageRequest.of(page, size, sort);

            Page<SplitActivityResponse> activities = splitActivityService.getGroupActivities(groupId, userId, pageable);
            return ResponseEntity.ok(ApiResponse.success(activities));

        } catch (Exception e) {
            return ResponseEntity.badRequest()
                .body(ApiResponse.error(e.getMessage()));
        }
    }

    /**
     * Get activities with a specific friend
     */
    @GetMapping("/friends/{friendId}")
    public ResponseEntity<ApiResponse<Page<SplitActivityResponse>>> getFriendActivities(
            @PathVariable Long friendId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size,
            @RequestParam(defaultValue = "createdAt") String sortBy,
            @RequestParam(defaultValue = "desc") String sortDir,
            HttpServletRequest httpRequest) {
        try {
            Long userId = (Long) httpRequest.getAttribute("userId");
            if (userId == null) {
                return ResponseEntity.badRequest()
                    .body(ApiResponse.error("User not authenticated"));
            }

            Sort sort = Sort.by(sortDir.equalsIgnoreCase("desc") ?
                Sort.Direction.DESC : Sort.Direction.ASC, sortBy);
            Pageable pageable = PageRequest.of(page, size, sort);

            Page<SplitActivityResponse> activities = splitActivityService.getFriendActivities(userId, friendId, pageable);
            return ResponseEntity.ok(ApiResponse.success(activities));

        } catch (Exception e) {
            return ResponseEntity.badRequest()
                .body(ApiResponse.error(e.getMessage()));
        }
    }

    /**
     * Get activity summary for dashboard
     */
    @GetMapping("/summary")
    public ResponseEntity<ApiResponse<ActivitySummaryResponse>> getActivitySummary(
            HttpServletRequest httpRequest) {
        try {
            Long userId = (Long) httpRequest.getAttribute("userId");
            if (userId == null) {
                return ResponseEntity.badRequest()
                    .body(ApiResponse.error("User not authenticated"));
            }

            // Get recent activities and create summary
            List<SplitActivityResponse> recentActivities = splitActivityService.getRecentActivities(userId, 50);

            ActivitySummaryResponse summary = new ActivitySummaryResponse();
            summary.setTotalActivities(recentActivities.size());
            summary.setRecentActivities(recentActivities.stream().limit(5).toList());

            // Count by type
            long groupActivities = recentActivities.stream()
                .filter(a -> a.getActivityType().name().startsWith("GROUP") ||
                           a.getActivityType().name().startsWith("MEMBER"))
                .count();

            long expenseActivities = recentActivities.stream()
                .filter(a -> a.getActivityType().name().startsWith("EXPENSE"))
                .count();

            long paymentActivities = recentActivities.stream()
                .filter(a -> a.getActivityType().name().contains("SETTLEMENT") ||
                           a.getActivityType().name().contains("PARTICIPANT"))
                .count();

            summary.setGroupActivitiesCount((int) groupActivities);
            summary.setExpenseActivitiesCount((int) expenseActivities);
            summary.setPaymentActivitiesCount((int) paymentActivities);

            return ResponseEntity.ok(ApiResponse.success(summary));

        } catch (Exception e) {
            return ResponseEntity.badRequest()
                .body(ApiResponse.error(e.getMessage()));
        }
    }

    // Helper response class for activity summary
    public static class ActivitySummaryResponse {
        private int totalActivities;
        private int groupActivitiesCount;
        private int expenseActivitiesCount;
        private int paymentActivitiesCount;
        private List<SplitActivityResponse> recentActivities;

        public int getTotalActivities() { return totalActivities; }
        public void setTotalActivities(int totalActivities) { this.totalActivities = totalActivities; }

        public int getGroupActivitiesCount() { return groupActivitiesCount; }
        public void setGroupActivitiesCount(int groupActivitiesCount) { this.groupActivitiesCount = groupActivitiesCount; }

        public int getExpenseActivitiesCount() { return expenseActivitiesCount; }
        public void setExpenseActivitiesCount(int expenseActivitiesCount) { this.expenseActivitiesCount = expenseActivitiesCount; }

        public int getPaymentActivitiesCount() { return paymentActivitiesCount; }
        public void setPaymentActivitiesCount(int paymentActivitiesCount) { this.paymentActivitiesCount = paymentActivitiesCount; }

        public List<SplitActivityResponse> getRecentActivities() { return recentActivities; }
        public void setRecentActivities(List<SplitActivityResponse> recentActivities) { this.recentActivities = recentActivities; }
    }
}