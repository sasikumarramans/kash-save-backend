package com.evbooking.backend.infrastructure.repository.split;

import com.evbooking.backend.domain.model.split.GroupMember;
import com.evbooking.backend.domain.repository.split.GroupMemberRepository;
import com.evbooking.backend.infrastructure.mapper.split.GroupMemberMapper;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public class GroupMemberRepositoryImpl implements GroupMemberRepository {

    private final JpaGroupMemberRepository jpaGroupMemberRepository;
    private final GroupMemberMapper groupMemberMapper;

    public GroupMemberRepositoryImpl(JpaGroupMemberRepository jpaGroupMemberRepository, GroupMemberMapper groupMemberMapper) {
        this.jpaGroupMemberRepository = jpaGroupMemberRepository;
        this.groupMemberMapper = groupMemberMapper;
    }

    @Override
    public GroupMember save(GroupMember groupMember) {
        var entity = groupMemberMapper.toEntity(groupMember);
        var savedEntity = jpaGroupMemberRepository.save(entity);
        return groupMemberMapper.toDomain(savedEntity);
    }

    @Override
    public List<GroupMember> findByGroupId(Long groupId) {
        return jpaGroupMemberRepository.findByGroupId(groupId)
                .stream()
                .map(groupMemberMapper::toDomain)
                .toList();
    }

    @Override
    public List<GroupMember> findByUserId(Long userId) {
        return jpaGroupMemberRepository.findByUserId(userId)
                .stream()
                .map(groupMemberMapper::toDomain)
                .toList();
    }

    @Override
    public Optional<GroupMember> findByGroupIdAndUserId(Long groupId, Long userId) {
        return jpaGroupMemberRepository.findByGroupIdAndUserId(groupId, userId)
                .map(groupMemberMapper::toDomain);
    }

    @Override
    public List<GroupMember> findByGroupIdAndIsAdminTrue(Long groupId) {
        return jpaGroupMemberRepository.findByGroupIdAndIsAdminTrue(groupId)
                .stream()
                .map(groupMemberMapper::toDomain)
                .toList();
    }

    @Override
    public void deleteByGroupIdAndUserId(Long groupId, Long userId) {
        jpaGroupMemberRepository.deleteByGroupIdAndUserId(groupId, userId);
    }

    @Override
    public void deleteByGroupId(Long groupId) {
        jpaGroupMemberRepository.deleteByGroupId(groupId);
    }

    @Override
    public List<Long> findUserIdsByGroupId(Long groupId) {
        return jpaGroupMemberRepository.findUserIdsByGroupId(groupId);
    }
}