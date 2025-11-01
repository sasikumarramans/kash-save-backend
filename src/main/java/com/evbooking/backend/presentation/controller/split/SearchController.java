package com.evbooking.backend.presentation.controller.split;

import com.evbooking.backend.domain.model.User;
import com.evbooking.backend.domain.model.split.SplitExpense;
import com.evbooking.backend.domain.model.split.SplitParticipant;
import com.evbooking.backend.domain.repository.UserRepository;
import com.evbooking.backend.domain.repository.split.*;
import com.evbooking.backend.infrastructure.entity.split.GroupEntity;
import com.evbooking.backend.infrastructure.entity.split.GroupMemberEntity;
import com.evbooking.backend.infrastructure.mapper.split.GroupMapper;
import com.evbooking.backend.infrastructure.mapper.split.SplitExpenseMapper;
import com.evbooking.backend.infrastructure.mapper.split.SplitParticipantMapper;
import com.evbooking.backend.presentation.dto.ApiResponse;
import com.evbooking.backend.presentation.dto.split.SearchResultResponse;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/search")
public class SearchController {

    private final UserRepository userRepository;
    private final GroupRepository groupRepository;
    private final GroupMemberRepository groupMemberRepository;
    private final SplitExpenseRepository splitExpenseRepository;
    private final SplitParticipantRepository splitParticipantRepository;
    private final SplitExpenseMapper splitExpenseMapper;
    private final SplitParticipantMapper splitParticipantMapper;
    private final GroupMapper groupMapper;

    public SearchController(UserRepository userRepository,
                           GroupRepository groupRepository,
                           GroupMemberRepository groupMemberRepository,
                           SplitExpenseRepository splitExpenseRepository,
                           SplitParticipantRepository splitParticipantRepository,
                           SplitExpenseMapper splitExpenseMapper,
                           SplitParticipantMapper splitParticipantMapper,
                           GroupMapper groupMapper) {
        this.userRepository = userRepository;
        this.groupRepository = groupRepository;
        this.groupMemberRepository = groupMemberRepository;
        this.splitExpenseRepository = splitExpenseRepository;
        this.splitParticipantRepository = splitParticipantRepository;
        this.splitExpenseMapper = splitExpenseMapper;
        this.splitParticipantMapper = splitParticipantMapper;
        this.groupMapper = groupMapper;
    }

    /**
     * Unified search endpoint for friends and groups
     * If query is empty, returns recent friends and groups
     *
     * @param query Search query (optional)
     * @param friendsLimit Max friends to return (default 10)
     * @param groupsLimit Max groups to return (default 10)
     */
    @GetMapping
    public ResponseEntity<ApiResponse<SearchResultResponse>> search(
            @RequestParam(required = false) String query,
            @RequestParam(defaultValue = "10") int friendsLimit,
            @RequestParam(defaultValue = "10") int groupsLimit,
            HttpServletRequest httpRequest) {
        try {
            String userId = (String) httpRequest.getAttribute("userId");
            if (userId == null) {
                return ResponseEntity.badRequest()
                    .body(ApiResponse.error("User not authenticated"));
            }

            SearchResultResponse response;

            if (query == null || query.trim().isEmpty()) {
                // Show recents
                response = getRecents(userId, friendsLimit, groupsLimit);
            } else {
                // Perform search
                response = performSearch(userId, query.trim(), friendsLimit, groupsLimit);
            }

            return ResponseEntity.ok(ApiResponse.success(response));

        } catch (Exception e) {
            return ResponseEntity.badRequest()
                .body(ApiResponse.error(e.getMessage()));
        }
    }

    /**
     * Get recently collaborated friends and recently active groups
     */
    private SearchResultResponse getRecents(String userId, int friendsLimit, int groupsLimit) {
        // Get recent friends (users with recent individual expenses)
        List<SearchResultResponse.FriendSearchResult> recentFriends = getRecentFriends(userId, friendsLimit);

        // Get recent groups (groups with recent activity or recently joined)
        List<SearchResultResponse.GroupSearchResult> recentGroups = getRecentGroups(userId, groupsLimit);

        return new SearchResultResponse(
            recentFriends,
            recentGroups,
            recentFriends.size(),
            recentGroups.size()
        );
    }

    /**
     * Search friends and groups by query
     */
    private SearchResultResponse performSearch(String userId, String query, int friendsLimit, int groupsLimit) {
        // Search friends by username or name
        List<SearchResultResponse.FriendSearchResult> friends = searchFriends(userId, query, friendsLimit);

        // Search groups by name
        List<SearchResultResponse.GroupSearchResult> groups = searchGroups(userId, query, groupsLimit);

        return new SearchResultResponse(
            friends,
            groups,
            friends.size(),
            groups.size()
        );
    }

    /**
     * Get recently collaborated friends (from ANY expense - individual or group)
     */
    private List<SearchResultResponse.FriendSearchResult> getRecentFriends(String userId, int limit) {
        // Get ALL expenses where user is involved, ordered by most recent
        var expenseEntities = splitExpenseRepository.findExpensesByParticipantUserId(userId);
        List<SplitExpense> allExpenses = expenseEntities.stream()
            .map(splitExpenseMapper::toDomain)
            .sorted((e1, e2) -> e2.getCreatedAt().compareTo(e1.getCreatedAt()))
            .collect(Collectors.toList());

        // Track friends and their last collaboration
        Map<String, LocalDateTime> friendActivity = new LinkedHashMap<>();

        for (SplitExpense expense : allExpenses) {
            var participantEntities = splitParticipantRepository.findBySplitExpenseId(expense.getId());
            for (var pEntity : participantEntities) {
                SplitParticipant participant = splitParticipantMapper.toDomain(pEntity);
                if (!participant.getUserId().equals(userId)) {
                    // This is a friend (anyone who shared an expense with current user)
                    friendActivity.putIfAbsent(participant.getUserId(), expense.getCreatedAt());
                }
            }

            if (friendActivity.size() >= limit) {
                break;
            }
        }

        // Convert to response
        List<SearchResultResponse.FriendSearchResult> results = new ArrayList<>();
        for (Map.Entry<String, LocalDateTime> entry : friendActivity.entrySet()) {
            Optional<User> userOpt = userRepository.findById(entry.getKey());
            if (userOpt.isPresent()) {
                User friend = userOpt.get();
                String fullName = (friend.getFirstName() != null ? friend.getFirstName() : "") +
                                 (friend.getLastName() != null ? " " + friend.getLastName() : "");
                results.add(new SearchResultResponse.FriendSearchResult(
                    friend.getId(),
                    friend.getUsername(),
                    fullName.trim(),
                    friend.getEmail(),
                    "Recent expense collaboration"
                ));
            }

            if (results.size() >= limit) {
                break;
            }
        }

        return results;
    }

    /**
     * Get recently active groups
     */
    private List<SearchResultResponse.GroupSearchResult> getRecentGroups(String userId, int limit) {
        // Get user's groups ordered by most recent activity
        Pageable pageable = PageRequest.of(0, limit, Sort.by(Sort.Direction.DESC, "updatedAt"));
        var groupEntities = groupRepository.findGroupsByMemberId(userId, pageable);

        List<SearchResultResponse.GroupSearchResult> results = new ArrayList<>();

        for (GroupEntity groupEntity : groupEntities) {
            // Get members
            var memberEntities = groupMemberRepository.findByGroupId(groupEntity.getId());

            // Build participant list
            List<SearchResultResponse.GroupMemberInfo> participants = new ArrayList<>();
            for (var memberEntity : memberEntities) {
                Optional<User> memberUserOpt = userRepository.findById(memberEntity.getUserId());
                if (memberUserOpt.isPresent()) {
                    User memberUser = memberUserOpt.get();
                    String fullName = (memberUser.getFirstName() != null ? memberUser.getFirstName() : "") +
                                     (memberUser.getLastName() != null ? " " + memberUser.getLastName() : "");
                    participants.add(new SearchResultResponse.GroupMemberInfo(
                        memberUser.getId(),
                        memberUser.getUsername(),
                        fullName.trim(),
                        memberEntity.isAdmin()
                    ));
                }
            }

            // Get latest expense for this group
            var groupExpenses = splitExpenseRepository.findByGroupId(groupEntity.getId());
            String lastActivity = groupExpenses.isEmpty() ? "No recent expenses" :
                "Latest: " + groupExpenses.get(0).getDescription();

            results.add(new SearchResultResponse.GroupSearchResult(
                groupEntity.getId(),
                groupEntity.getName(),
                groupEntity.getDescription(),
                memberEntities.size(),
                participants,
                lastActivity
            ));
        }

        return results;
    }

    /**
     * Search friends by username or name (from ANY expense - individual or group)
     */
    private List<SearchResultResponse.FriendSearchResult> searchFriends(String userId, String query, int limit) {
        // Get ALL users who have shared expenses with current user (individual or group)
        var expenseEntities = splitExpenseRepository.findExpensesByParticipantUserId(userId);
        List<SplitExpense> allExpenses = expenseEntities.stream()
            .map(splitExpenseMapper::toDomain)
            .collect(Collectors.toList());

        // Collect all friend IDs
        Set<String> friendIds = new HashSet<>();
        for (SplitExpense expense : allExpenses) {
            var participantEntities = splitParticipantRepository.findBySplitExpenseId(expense.getId());
            for (var pEntity : participantEntities) {
                SplitParticipant participant = splitParticipantMapper.toDomain(pEntity);
                if (!participant.getUserId().equals(userId)) {
                    friendIds.add(participant.getUserId());
                }
            }
        }

        // Search among friends by username or name
        String lowerQuery = query.toLowerCase();
        List<SearchResultResponse.FriendSearchResult> results = new ArrayList<>();

        for (String friendId : friendIds) {
            Optional<User> userOpt = userRepository.findById(friendId);
            if (userOpt.isPresent()) {
                User friend = userOpt.get();
                String fullName = (friend.getFirstName() != null ? friend.getFirstName() : "") +
                                 (friend.getLastName() != null ? " " + friend.getLastName() : "");
                if (friend.getUsername().toLowerCase().contains(lowerQuery) ||
                    fullName.toLowerCase().contains(lowerQuery) ||
                    (friend.getEmail() != null && friend.getEmail().toLowerCase().contains(lowerQuery))) {
                    results.add(new SearchResultResponse.FriendSearchResult(
                        friend.getId(),
                        friend.getUsername(),
                        fullName.trim(),
                        friend.getEmail(),
                        "Expense collaborator"
                    ));
                }
            }

            if (results.size() >= limit) {
                break;
            }
        }

        return results;
    }

    /**
     * Search groups by name, description, or member name
     */
    private List<SearchResultResponse.GroupSearchResult> searchGroups(String userId, String query, int limit) {
        // Get all user's groups
        Pageable pageable = PageRequest.of(0, 100); // Get more initially to filter
        var groupEntities = groupRepository.findGroupsByMemberId(userId, pageable);

        String lowerQuery = query.toLowerCase();
        List<SearchResultResponse.GroupSearchResult> results = new ArrayList<>();

        for (GroupEntity groupEntity : groupEntities) {
            // Get members
            var memberEntities = groupMemberRepository.findByGroupId(groupEntity.getId());

            // Build participant list
            List<SearchResultResponse.GroupMemberInfo> participants = new ArrayList<>();
            boolean memberNameMatches = false;

            for (var memberEntity : memberEntities) {
                Optional<User> memberUserOpt = userRepository.findById(memberEntity.getUserId());
                if (memberUserOpt.isPresent()) {
                    User memberUser = memberUserOpt.get();
                    String fullName = (memberUser.getFirstName() != null ? memberUser.getFirstName() : "") +
                                     (memberUser.getLastName() != null ? " " + memberUser.getLastName() : "");

                    // Check if this member's name matches the query
                    if (memberUser.getUsername().toLowerCase().contains(lowerQuery) ||
                        fullName.toLowerCase().contains(lowerQuery)) {
                        memberNameMatches = true;
                    }

                    participants.add(new SearchResultResponse.GroupMemberInfo(
                        memberUser.getId(),
                        memberUser.getUsername(),
                        fullName.trim(),
                        memberEntity.isAdmin()
                    ));
                }
            }

            // Match by group name, description, OR member name
            if (groupEntity.getName().toLowerCase().contains(lowerQuery) ||
                (groupEntity.getDescription() != null && groupEntity.getDescription().toLowerCase().contains(lowerQuery)) ||
                memberNameMatches) {

                // Get latest expense for this group
                var groupExpenses = splitExpenseRepository.findByGroupId(groupEntity.getId());
                String lastActivity = groupExpenses.isEmpty() ? "No expenses yet" :
                    "Latest: " + groupExpenses.get(0).getDescription();

                results.add(new SearchResultResponse.GroupSearchResult(
                    groupEntity.getId(),
                    groupEntity.getName(),
                    groupEntity.getDescription(),
                    memberEntities.size(),
                    participants,
                    lastActivity
                ));
            }

            if (results.size() >= limit) {
                break;
            }
        }

        return results;
    }
}