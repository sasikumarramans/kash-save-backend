package com.evbooking.backend.domain.repository.split;

import com.evbooking.backend.domain.model.split.GroupMember;

import java.util.List;
import java.util.Optional;

public interface GroupMemberRepository {
    GroupMember save(GroupMember groupMember);
    List<GroupMember> findByGroupId(Long groupId);
    List<GroupMember> findByUserId(Long userId);
    Optional<GroupMember> findByGroupIdAndUserId(Long groupId, Long userId);
    List<GroupMember> findByGroupIdAndIsAdminTrue(Long groupId);
    void deleteByGroupIdAndUserId(Long groupId, Long userId);
    void deleteByGroupId(Long groupId);
    List<Long> findUserIdsByGroupId(Long groupId);
}