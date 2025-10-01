package com.evbooking.backend.presentation.controller.split;

import com.evbooking.backend.domain.model.split.Group;
import com.evbooking.backend.domain.model.split.GroupMember;
import com.evbooking.backend.domain.model.User;
import com.evbooking.backend.domain.repository.UserRepository;
import com.evbooking.backend.presentation.dto.ApiResponse;
import com.evbooking.backend.presentation.dto.split.*;
import com.evbooking.backend.usecase.service.split.GroupService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/groups")
public class GroupController {

    private final GroupService groupService;
    private final UserRepository userRepository;

    public GroupController(GroupService groupService, UserRepository userRepository) {
        this.groupService = groupService;
        this.userRepository = userRepository;
    }

    @PostMapping
    public ResponseEntity<ApiResponse<GroupResponse>> createGroup(@Valid @RequestBody CreateGroupRequest request,
                                                                 HttpServletRequest httpRequest) {
        try {
            Long userId = (Long) httpRequest.getAttribute("userId");
            if (userId == null) {
                return ResponseEntity.badRequest()
                    .body(ApiResponse.error("User not authenticated"));
            }

            Group group = groupService.createGroup(
                request.getName(),
                request.getDescription(),
                request.getCurrency(),
                request.getMemberUsernames(),
                userId
            );

            GroupResponse response = convertToGroupResponse(group);
            return ResponseEntity.ok(ApiResponse.success(response));

        } catch (Exception e) {
            return ResponseEntity.badRequest()
                .body(ApiResponse.error(e.getMessage()));
        }
    }

    @GetMapping
    public ResponseEntity<ApiResponse<Page<GroupResponse>>> getUserGroups(
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

            Page<Group> groups = groupService.getUserGroups(userId, pageable);
            Page<GroupResponse> response = groups.map(this::convertToGroupResponse);

            return ResponseEntity.ok(ApiResponse.success(response));

        } catch (Exception e) {
            return ResponseEntity.badRequest()
                .body(ApiResponse.error(e.getMessage()));
        }
    }

    @GetMapping("/{groupId}")
    public ResponseEntity<ApiResponse<GroupResponse>> getGroup(@PathVariable Long groupId,
                                                              HttpServletRequest httpRequest) {
        try {
            Long userId = (Long) httpRequest.getAttribute("userId");
            if (userId == null) {
                return ResponseEntity.badRequest()
                    .body(ApiResponse.error("User not authenticated"));
            }

            Optional<Group> groupOpt = groupService.getGroupById(groupId, userId);
            if (groupOpt.isEmpty()) {
                return ResponseEntity.notFound().build();
            }

            GroupResponse response = convertToGroupResponse(groupOpt.get());
            return ResponseEntity.ok(ApiResponse.success(response));

        } catch (Exception e) {
            return ResponseEntity.badRequest()
                .body(ApiResponse.error(e.getMessage()));
        }
    }

    @GetMapping("/{groupId}/members")
    public ResponseEntity<ApiResponse<List<GroupMemberResponse>>> getGroupMembers(@PathVariable Long groupId,
                                                                                 HttpServletRequest httpRequest) {
        try {
            Long userId = (Long) httpRequest.getAttribute("userId");
            if (userId == null) {
                return ResponseEntity.badRequest()
                    .body(ApiResponse.error("User not authenticated"));
            }

            List<GroupMember> members = groupService.getGroupMembers(groupId, userId);
            List<GroupMemberResponse> response = members.stream()
                .map(this::convertToGroupMemberResponse)
                .collect(Collectors.toList());

            return ResponseEntity.ok(ApiResponse.success(response));

        } catch (Exception e) {
            return ResponseEntity.badRequest()
                .body(ApiResponse.error(e.getMessage()));
        }
    }

    @PostMapping("/{groupId}/members")
    public ResponseEntity<ApiResponse<GroupMemberResponse>> addMember(@PathVariable Long groupId,
                                                                     @RequestBody AddMemberRequest request,
                                                                     HttpServletRequest httpRequest) {
        try {
            Long userId = (Long) httpRequest.getAttribute("userId");
            if (userId == null) {
                return ResponseEntity.badRequest()
                    .body(ApiResponse.error("User not authenticated"));
            }

            GroupMember member = groupService.addMemberToGroup(groupId, request.getUsername(), userId);
            GroupMemberResponse response = convertToGroupMemberResponse(member);

            return ResponseEntity.ok(ApiResponse.success(response));

        } catch (Exception e) {
            return ResponseEntity.badRequest()
                .body(ApiResponse.error(e.getMessage()));
        }
    }

    @DeleteMapping("/{groupId}/members/{memberUserId}")
    public ResponseEntity<ApiResponse<String>> removeMember(@PathVariable Long groupId,
                                                           @PathVariable Long memberUserId,
                                                           HttpServletRequest httpRequest) {
        try {
            Long userId = (Long) httpRequest.getAttribute("userId");
            if (userId == null) {
                return ResponseEntity.badRequest()
                    .body(ApiResponse.error("User not authenticated"));
            }

            groupService.removeMemberFromGroup(groupId, memberUserId, userId);
            return ResponseEntity.ok(ApiResponse.success("Member removed successfully"));

        } catch (Exception e) {
            return ResponseEntity.badRequest()
                .body(ApiResponse.error(e.getMessage()));
        }
    }

    @PostMapping("/{groupId}/leave")
    public ResponseEntity<ApiResponse<String>> leaveGroup(@PathVariable Long groupId,
                                                         HttpServletRequest httpRequest) {
        try {
            Long userId = (Long) httpRequest.getAttribute("userId");
            if (userId == null) {
                return ResponseEntity.badRequest()
                    .body(ApiResponse.error("User not authenticated"));
            }

            groupService.leaveGroup(groupId, userId);
            return ResponseEntity.ok(ApiResponse.success("Left group successfully"));

        } catch (Exception e) {
            return ResponseEntity.badRequest()
                .body(ApiResponse.error(e.getMessage()));
        }
    }

    @DeleteMapping("/{groupId}")
    public ResponseEntity<ApiResponse<String>> deleteGroup(@PathVariable Long groupId,
                                                          HttpServletRequest httpRequest) {
        try {
            Long userId = (Long) httpRequest.getAttribute("userId");
            if (userId == null) {
                return ResponseEntity.badRequest()
                    .body(ApiResponse.error("User not authenticated"));
            }

            groupService.deleteGroup(groupId, userId);
            return ResponseEntity.ok(ApiResponse.success("Group deleted successfully"));

        } catch (Exception e) {
            return ResponseEntity.badRequest()
                .body(ApiResponse.error(e.getMessage()));
        }
    }

    @PutMapping("/{groupId}/admin/{memberUserId}")
    public ResponseEntity<ApiResponse<GroupMemberResponse>> makeAdmin(@PathVariable Long groupId,
                                                                     @PathVariable Long memberUserId,
                                                                     HttpServletRequest httpRequest) {
        try {
            Long userId = (Long) httpRequest.getAttribute("userId");
            if (userId == null) {
                return ResponseEntity.badRequest()
                    .body(ApiResponse.error("User not authenticated"));
            }

            GroupMember member = groupService.makeAdmin(groupId, memberUserId, userId);
            GroupMemberResponse response = convertToGroupMemberResponse(member);

            return ResponseEntity.ok(ApiResponse.success(response));

        } catch (Exception e) {
            return ResponseEntity.badRequest()
                .body(ApiResponse.error(e.getMessage()));
        }
    }

    @PutMapping("/{groupId}")
    public ResponseEntity<ApiResponse<GroupResponse>> updateGroup(@PathVariable Long groupId,
                                                                 @RequestBody UpdateGroupRequest request,
                                                                 HttpServletRequest httpRequest) {
        try {
            Long userId = (Long) httpRequest.getAttribute("userId");
            if (userId == null) {
                return ResponseEntity.badRequest()
                    .body(ApiResponse.error("User not authenticated"));
            }

            Group group = groupService.updateGroup(groupId, request.getName(),
                request.getDescription(), request.getCurrency(), userId);
            GroupResponse response = convertToGroupResponse(group);

            return ResponseEntity.ok(ApiResponse.success(response));

        } catch (Exception e) {
            return ResponseEntity.badRequest()
                .body(ApiResponse.error(e.getMessage()));
        }
    }

    private GroupResponse convertToGroupResponse(Group group) {
        // Get admin user details
        Optional<User> adminUserOpt = userRepository.findById(group.getAdminUserId());
        String adminUsername = adminUserOpt.map(User::getUsername).orElse("Unknown");

        // Get member count (simplified for now)
        int memberCount = 0; // Will be populated when we call with members

        return new GroupResponse(
            group.getId(),
            group.getName(),
            group.getDescription(),
            group.getCurrency(),
            group.getAdminUserId(),
            adminUsername,
            memberCount,
            null, // Members list will be populated separately if needed
            group.getCreatedAt()
        );
    }

    private GroupMemberResponse convertToGroupMemberResponse(GroupMember member) {
        Optional<User> userOpt = userRepository.findById(member.getUserId());
        if (userOpt.isEmpty()) {
            throw new RuntimeException("User not found for member");
        }

        User user = userOpt.get();
        return new GroupMemberResponse(
            user.getId(),
            user.getUsername(),
            user.getEmail(),
            member.isAdmin(),
            member.getJoinedAt()
        );
    }

    // Helper DTOs
    public static class AddMemberRequest {
        private String username;

        public String getUsername() { return username; }
        public void setUsername(String username) { this.username = username; }
    }

    public static class UpdateGroupRequest {
        private String name;
        private String description;
        private String currency;

        public String getName() { return name; }
        public void setName(String name) { this.name = name; }

        public String getDescription() { return description; }
        public void setDescription(String description) { this.description = description; }

        public String getCurrency() { return currency; }
        public void setCurrency(String currency) { this.currency = currency; }
    }
}