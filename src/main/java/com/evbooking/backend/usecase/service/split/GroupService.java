package com.evbooking.backend.usecase.service.split;

import com.evbooking.backend.domain.model.split.Group;
import com.evbooking.backend.domain.model.split.GroupMember;
import com.evbooking.backend.domain.model.User;
import com.evbooking.backend.domain.repository.split.GroupRepository;
import com.evbooking.backend.domain.repository.split.GroupMemberRepository;
import com.evbooking.backend.domain.repository.UserRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Service
@Transactional
public class GroupService {

    private final GroupRepository groupRepository;
    private final GroupMemberRepository groupMemberRepository;
    private final UserRepository userRepository;
    private final SplitActivityService splitActivityService;

    public GroupService(GroupRepository groupRepository, GroupMemberRepository groupMemberRepository, UserRepository userRepository, SplitActivityService splitActivityService) {
        this.groupRepository = groupRepository;
        this.groupMemberRepository = groupMemberRepository;
        this.userRepository = userRepository;
        this.splitActivityService = splitActivityService;
    }

    public Group createGroup(String name, String description, String currency, List<String> memberUsernames, Long adminUserId) {
        // Validate admin user exists
        if (!userRepository.findById(adminUserId).isPresent()) {
            throw new RuntimeException("Admin user not found");
        }

        // Create group
        Group group = new Group(name, description, adminUserId, currency);
        group = groupRepository.save(group);

        // Add admin as member
        GroupMember adminMember = new GroupMember(group.getId(), adminUserId, true);
        groupMemberRepository.save(adminMember);

        // Add other members by username
        for (String username : memberUsernames) {
            Optional<User> userOpt = userRepository.findByUsername(username);
            if (userOpt.isEmpty()) {
                throw new RuntimeException("User not found with username: " + username);
            }

            User user = userOpt.get();
            // Skip if admin is already added
            if (!user.getId().equals(adminUserId)) {
                // Check if user is already a member
                if (!groupMemberRepository.existsByGroupIdAndUserId(group.getId(), user.getId())) {
                    GroupMember member = new GroupMember(group.getId(), user.getId(), false);
                    groupMemberRepository.save(member);

                    // Log member added activity
                    splitActivityService.logMemberAdded(group.getId(), user.getId(), adminUserId);
                }
            }
        }

        // Log group creation activity
        splitActivityService.logGroupCreated(group.getId(), adminUserId);

        return group;
    }

    public Optional<Group> getGroupById(Long groupId, Long userId) {
        // Check if user is a member of the group
        if (!groupMemberRepository.existsByGroupIdAndUserId(groupId, userId)) {
            return Optional.empty();
        }

        return groupRepository.findById(groupId);
    }

    public Page<Group> getUserGroups(Long userId, Pageable pageable) {
        return groupRepository.findGroupsByMemberId(userId, pageable);
    }

    public List<GroupMember> getGroupMembers(Long groupId, Long userId) {
        // Check if user is a member of the group
        if (!groupMemberRepository.existsByGroupIdAndUserId(groupId, userId)) {
            throw new RuntimeException("You are not a member of this group");
        }

        return groupMemberRepository.findByGroupId(groupId);
    }

    public GroupMember addMemberToGroup(Long groupId, String username, Long adminUserId) {
        // Verify admin permissions
        Optional<GroupMember> adminMemberOpt = groupMemberRepository.findByGroupIdAndUserId(groupId, adminUserId);
        if (adminMemberOpt.isEmpty() || !adminMemberOpt.get().isAdmin()) {
            throw new RuntimeException("Only group admins can add members");
        }

        // Find user by username
        Optional<User> userOpt = userRepository.findByUsername(username);
        if (userOpt.isEmpty()) {
            throw new RuntimeException("User not found with username: " + username);
        }

        User user = userOpt.get();

        // Check if user is already a member
        if (groupMemberRepository.existsByGroupIdAndUserId(groupId, user.getId())) {
            throw new RuntimeException("User is already a member of this group");
        }

        GroupMember member = new GroupMember(groupId, user.getId(), false);
        GroupMember savedMember = groupMemberRepository.save(member);

        // Log member added activity
        splitActivityService.logMemberAdded(groupId, user.getId(), adminUserId);

        return savedMember;
    }

    public void removeMemberFromGroup(Long groupId, Long userIdToRemove, Long adminUserId) {
        // Verify admin permissions
        Optional<GroupMember> adminMemberOpt = groupMemberRepository.findByGroupIdAndUserId(groupId, adminUserId);
        if (adminMemberOpt.isEmpty() || !adminMemberOpt.get().isAdmin()) {
            throw new RuntimeException("Only group admins can remove members");
        }

        // Check if member exists
        if (!groupMemberRepository.existsByGroupIdAndUserId(groupId, userIdToRemove)) {
            throw new RuntimeException("User is not a member of this group");
        }

        // Prevent removing the last admin
        List<GroupMember> admins = groupMemberRepository.findAdminsByGroupId(groupId);
        if (admins.size() == 1 && admins.get(0).getUserId().equals(userIdToRemove)) {
            throw new RuntimeException("Cannot remove the last admin from the group");
        }

        groupMemberRepository.deleteByGroupIdAndUserId(groupId, userIdToRemove);
    }

    public void leaveGroup(Long groupId, Long userId) {
        // Check if user is a member
        if (!groupMemberRepository.existsByGroupIdAndUserId(groupId, userId)) {
            throw new RuntimeException("You are not a member of this group");
        }

        // Check if user is the last admin
        List<GroupMember> admins = groupMemberRepository.findAdminsByGroupId(groupId);
        boolean isLastAdmin = admins.size() == 1 && admins.get(0).getUserId().equals(userId);

        if (isLastAdmin) {
            // Check if there are other members
            long memberCount = groupMemberRepository.countByGroupId(groupId);
            if (memberCount > 1) {
                throw new RuntimeException("Cannot leave group as the last admin. Please transfer admin rights first or delete the group.");
            } else {
                // Delete the entire group if last member
                deleteGroup(groupId, userId);
                return;
            }
        }

        groupMemberRepository.deleteByGroupIdAndUserId(groupId, userId);
    }

    public void deleteGroup(Long groupId, Long adminUserId) {
        // Verify admin permissions
        Optional<GroupMember> adminMemberOpt = groupMemberRepository.findByGroupIdAndUserId(groupId, adminUserId);
        if (adminMemberOpt.isEmpty() || !adminMemberOpt.get().isAdmin()) {
            throw new RuntimeException("Only group admins can delete the group");
        }

        // Check if group exists
        if (!groupRepository.findById(groupId).isPresent()) {
            throw new RuntimeException("Group not found");
        }

        groupRepository.deleteById(groupId);
    }

    public GroupMember makeAdmin(Long groupId, Long userIdToPromote, Long currentAdminUserId) {
        // Verify current admin permissions
        Optional<GroupMember> currentAdminOpt = groupMemberRepository.findByGroupIdAndUserId(groupId, currentAdminUserId);
        if (currentAdminOpt.isEmpty() || !currentAdminOpt.get().isAdmin()) {
            throw new RuntimeException("Only group admins can promote members");
        }

        // Find member to promote
        Optional<GroupMember> memberOpt = groupMemberRepository.findByGroupIdAndUserId(groupId, userIdToPromote);
        if (memberOpt.isEmpty()) {
            throw new RuntimeException("User is not a member of this group");
        }

        GroupMember member = memberOpt.get();
        if (member.isAdmin()) {
            throw new RuntimeException("User is already an admin");
        }

        member.setAdmin(true);
        return groupMemberRepository.save(member);
    }

    public Group updateGroup(Long groupId, String name, String description, String currency, Long adminUserId) {
        // Verify admin permissions
        Optional<GroupMember> adminMemberOpt = groupMemberRepository.findByGroupIdAndUserId(groupId, adminUserId);
        if (adminMemberOpt.isEmpty() || !adminMemberOpt.get().isAdmin()) {
            throw new RuntimeException("Only group admins can update the group");
        }

        Optional<Group> groupOpt = groupRepository.findById(groupId);
        if (groupOpt.isEmpty()) {
            throw new RuntimeException("Group not found");
        }

        Group group = groupOpt.get();
        if (name != null && !name.trim().isEmpty()) {
            group.setName(name.trim());
        }
        if (description != null) {
            group.setDescription(description);
        }
        if (currency != null && !currency.trim().isEmpty()) {
            group.setCurrency(currency.trim());
        }

        return groupRepository.save(group);
    }

    public boolean isGroupMember(Long groupId, Long userId) {
        return groupMemberRepository.existsByGroupIdAndUserId(groupId, userId);
    }

    public boolean isGroupAdmin(Long groupId, Long userId) {
        Optional<GroupMember> memberOpt = groupMemberRepository.findByGroupIdAndUserId(groupId, userId);
        return memberOpt.isPresent() && memberOpt.get().isAdmin();
    }
}