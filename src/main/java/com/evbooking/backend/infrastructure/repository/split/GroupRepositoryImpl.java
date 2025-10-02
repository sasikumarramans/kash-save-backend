package com.evbooking.backend.infrastructure.repository.split;

import com.evbooking.backend.domain.model.split.Group;
import com.evbooking.backend.domain.repository.split.GroupRepository;
import com.evbooking.backend.infrastructure.entity.split.GroupEntity;
import com.evbooking.backend.infrastructure.mapper.split.GroupMapper;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public class GroupRepositoryImpl implements GroupRepository {

    private final JpaGroupRepository jpaGroupRepository;
    private final GroupMapper groupMapper;

    public GroupRepositoryImpl(JpaGroupRepository jpaGroupRepository, GroupMapper groupMapper) {
        this.jpaGroupRepository = jpaGroupRepository;
        this.groupMapper = groupMapper;
    }

    @Override
    public Group save(Group group) {
        GroupEntity entity = groupMapper.toEntity(group);
        GroupEntity savedEntity = jpaGroupRepository.save(entity);
        return groupMapper.toDomain(savedEntity);
    }

    @Override
    public Optional<Group> findById(Long id) {
        return jpaGroupRepository.findById(id)
                .map(groupMapper::toDomain);
    }

    @Override
    public Page<Group> findByUserId(Long userId, Pageable pageable) {
        return jpaGroupRepository.findByUserId(userId, pageable)
                .map(groupMapper::toDomain);
    }

    @Override
    public Optional<Group> findByIdAndUserId(Long groupId, Long userId) {
        return jpaGroupRepository.findByIdAndUserId(groupId, userId)
                .map(groupMapper::toDomain);
    }

    @Override
    public List<Group> findByAdminUserId(Long adminUserId) {
        return jpaGroupRepository.findByAdminUserId(adminUserId)
                .stream()
                .map(groupMapper::toDomain)
                .toList();
    }

    @Override
    public void deleteById(Long id) {
        jpaGroupRepository.deleteById(id);
    }

    @Override
    public boolean existsById(Long id) {
        return jpaGroupRepository.existsById(id);
    }
}