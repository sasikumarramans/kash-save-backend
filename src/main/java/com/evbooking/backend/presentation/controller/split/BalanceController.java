package com.evbooking.backend.presentation.controller.split;

import com.evbooking.backend.presentation.dto.ApiResponse;
import com.evbooking.backend.presentation.dto.split.*;
import com.evbooking.backend.usecase.service.split.BalanceService;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/splits/balances")
public class BalanceController {

    private final BalanceService balanceService;

    public BalanceController(BalanceService balanceService) {
        this.balanceService = balanceService;
    }

    /**
     * Get friends balances with filtering options
     * @param filter Options: all, outstanding, you_owe, owes_you, settled
     */
    @GetMapping("/friends")
    public ResponseEntity<ApiResponse<List<FriendBalanceResponse>>> getFriendsBalances(
            @RequestParam(defaultValue = "all") String filter,
            HttpServletRequest httpRequest) {
        try {
            Long userId = (Long) httpRequest.getAttribute("userId");
            if (userId == null) {
                return ResponseEntity.badRequest()
                    .body(ApiResponse.error("User not authenticated"));
            }

            List<FriendBalanceResponse> balances = balanceService.getFriendsBalances(userId, filter);
            return ResponseEntity.ok(ApiResponse.success(balances));

        } catch (Exception e) {
            return ResponseEntity.badRequest()
                .body(ApiResponse.error(e.getMessage()));
        }
    }

    /**
     * Get groups balances with filtering options
     * @param filter Options: all, outstanding, you_owe, owes_you, settled
     */
    @GetMapping("/groups")
    public ResponseEntity<ApiResponse<List<GroupBalanceResponse>>> getGroupsBalances(
            @RequestParam(defaultValue = "all") String filter,
            HttpServletRequest httpRequest) {
        try {
            Long userId = (Long) httpRequest.getAttribute("userId");
            if (userId == null) {
                return ResponseEntity.badRequest()
                    .body(ApiResponse.error("User not authenticated"));
            }

            List<GroupBalanceResponse> balances = balanceService.getGroupsBalances(userId, filter);
            return ResponseEntity.ok(ApiResponse.success(balances));

        } catch (Exception e) {
            return ResponseEntity.badRequest()
                .body(ApiResponse.error(e.getMessage()));
        }
    }

    /**
     * Get overall balance summary across all friends and groups
     */
    @GetMapping("/summary")
    public ResponseEntity<ApiResponse<OverallBalanceSummaryResponse>> getOverallBalanceSummary(
            HttpServletRequest httpRequest) {
        try {
            Long userId = (Long) httpRequest.getAttribute("userId");
            if (userId == null) {
                return ResponseEntity.badRequest()
                    .body(ApiResponse.error("User not authenticated"));
            }

            OverallBalanceSummaryResponse summary = balanceService.getOverallBalanceSummary(userId);
            return ResponseEntity.ok(ApiResponse.success(summary));

        } catch (Exception e) {
            return ResponseEntity.badRequest()
                .body(ApiResponse.error(e.getMessage()));
        }
    }

    /**
     * Get friends balances with additional filtering options
     */
    @GetMapping("/friends/detailed")
    public ResponseEntity<ApiResponse<FriendsBalanceDetailedResponse>> getFriendsBalancesDetailed(
            @RequestParam(defaultValue = "all") String filter,
            @RequestParam(required = false) String currency,
            @RequestParam(defaultValue = "false") boolean includeSettled,
            HttpServletRequest httpRequest) {
        try {
            Long userId = (Long) httpRequest.getAttribute("userId");
            if (userId == null) {
                return ResponseEntity.badRequest()
                    .body(ApiResponse.error("User not authenticated"));
            }

            List<FriendBalanceResponse> balances = balanceService.getFriendsBalances(userId, filter);

            // Additional filtering by currency if specified
            if (currency != null && !currency.trim().isEmpty()) {
                balances = balances.stream()
                    .filter(balance -> currency.equals(balance.getCurrency()))
                    .toList();
            }

            // Filter out settled if not included
            if (!includeSettled) {
                balances = balances.stream()
                    .filter(balance -> !balance.isSettled())
                    .toList();
            }

            // Calculate summary for this filtered set
            FriendsBalanceDetailedResponse response = new FriendsBalanceDetailedResponse();
            response.setFriends(balances);
            response.setTotalFriends(balances.size());
            response.setFilter(filter);
            response.setCurrency(currency);

            return ResponseEntity.ok(ApiResponse.success(response));

        } catch (Exception e) {
            return ResponseEntity.badRequest()
                .body(ApiResponse.error(e.getMessage()));
        }
    }

    /**
     * Get groups balances with additional filtering options
     */
    @GetMapping("/groups/detailed")
    public ResponseEntity<ApiResponse<GroupsBalanceDetailedResponse>> getGroupsBalancesDetailed(
            @RequestParam(defaultValue = "all") String filter,
            @RequestParam(required = false) String currency,
            @RequestParam(defaultValue = "false") boolean includeSettled,
            HttpServletRequest httpRequest) {
        try {
            Long userId = (Long) httpRequest.getAttribute("userId");
            if (userId == null) {
                return ResponseEntity.badRequest()
                    .body(ApiResponse.error("User not authenticated"));
            }

            List<GroupBalanceResponse> balances = balanceService.getGroupsBalances(userId, filter);

            // Additional filtering by currency if specified
            if (currency != null && !currency.trim().isEmpty()) {
                balances = balances.stream()
                    .filter(balance -> currency.equals(balance.getCurrency()))
                    .toList();
            }

            // Filter out settled if not included
            if (!includeSettled) {
                balances = balances.stream()
                    .filter(balance -> !balance.isSettled())
                    .toList();
            }

            GroupsBalanceDetailedResponse response = new GroupsBalanceDetailedResponse();
            response.setGroups(balances);
            response.setTotalGroups(balances.size());
            response.setFilter(filter);
            response.setCurrency(currency);

            return ResponseEntity.ok(ApiResponse.success(response));

        } catch (Exception e) {
            return ResponseEntity.badRequest()
                .body(ApiResponse.error(e.getMessage()));
        }
    }

    // Helper response classes for detailed endpoints
    public static class FriendsBalanceDetailedResponse {
        private List<FriendBalanceResponse> friends;
        private int totalFriends;
        private String filter;
        private String currency;

        public List<FriendBalanceResponse> getFriends() { return friends; }
        public void setFriends(List<FriendBalanceResponse> friends) { this.friends = friends; }

        public int getTotalFriends() { return totalFriends; }
        public void setTotalFriends(int totalFriends) { this.totalFriends = totalFriends; }

        public String getFilter() { return filter; }
        public void setFilter(String filter) { this.filter = filter; }

        public String getCurrency() { return currency; }
        public void setCurrency(String currency) { this.currency = currency; }
    }

    public static class GroupsBalanceDetailedResponse {
        private List<GroupBalanceResponse> groups;
        private int totalGroups;
        private String filter;
        private String currency;

        public List<GroupBalanceResponse> getGroups() { return groups; }
        public void setGroups(List<GroupBalanceResponse> groups) { this.groups = groups; }

        public int getTotalGroups() { return totalGroups; }
        public void setTotalGroups(int totalGroups) { this.totalGroups = totalGroups; }

        public String getFilter() { return filter; }
        public void setFilter(String filter) { this.filter = filter; }

        public String getCurrency() { return currency; }
        public void setCurrency(String currency) { this.currency = currency; }
    }
}