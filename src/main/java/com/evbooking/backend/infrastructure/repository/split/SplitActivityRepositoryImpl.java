package com.evbooking.backend.infrastructure.repository.split;

import com.evbooking.backend.domain.model.split.SplitActivity;
import com.evbooking.backend.domain.model.split.SplitActivityType;
import com.evbooking.backend.domain.repository.split.SplitActivityRepository;
import com.evbooking.backend.infrastructure.entity.split.SplitActivityEntity;
import com.evbooking.backend.infrastructure.jpa.split.SplitActivityJpaRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Repository
public class SplitActivityRepositoryImpl implements SplitActivityRepository {

    private final SplitActivityJpaRepository jpaRepository;

    public SplitActivityRepositoryImpl(SplitActivityJpaRepository jpaRepository) {
        this.jpaRepository = jpaRepository;
    }

    @Override
    public SplitActivity save(SplitActivity activity) {
        SplitActivityEntity entity = toEntity(activity);
        SplitActivityEntity saved = jpaRepository.save(entity);
        return toDomain(saved);
    }

    @Override
    public Optional<SplitActivity> findById(Long id) {
        return jpaRepository.findById(id)
            .map(this::toDomain);
    }

    @Override
    public Page<SplitActivity> findActivitiesForUser(Long userId, Pageable pageable) {
        Page<SplitActivityEntity> entities = jpaRepository.findActivitiesForUser(userId, pageable);
        List<SplitActivity> activities = entities.getContent().stream()
            .map(this::toDomain)
            .collect(Collectors.toList());
        return new PageImpl<>(activities, pageable, entities.getTotalElements());
    }

    @Override
    public List<SplitActivity> findActivitiesForUser(Long userId) {
        List<SplitActivityEntity> entities = jpaRepository.findActivitiesForUser(userId);
        return entities.stream()
            .map(this::toDomain)
            .collect(Collectors.toList());
    }

    @Override
    public Page<SplitActivity> findByActivityTypeAndUser(SplitActivityType activityType, Long userId, Pageable pageable) {
        Page<SplitActivityEntity> entities = jpaRepository.findByActivityTypeAndUser(activityType, userId, pageable);
        List<SplitActivity> activities = entities.getContent().stream()
            .map(this::toDomain)
            .collect(Collectors.toList());
        return new PageImpl<>(activities, pageable, entities.getTotalElements());
    }

    @Override
    public Page<SplitActivity> findByCreatedAtBetweenAndUser(LocalDateTime startDate, LocalDateTime endDate, Long userId, Pageable pageable) {
        Page<SplitActivityEntity> entities = jpaRepository.findByCreatedAtBetweenAndUser(startDate, endDate, userId, pageable);
        List<SplitActivity> activities = entities.getContent().stream()
            .map(this::toDomain)
            .collect(Collectors.toList());
        return new PageImpl<>(activities, pageable, entities.getTotalElements());
    }

    @Override
    public Page<SplitActivity> findByGroupIdAndUser(Long groupId, Long userId, Pageable pageable) {
        Page<SplitActivityEntity> entities = jpaRepository.findByGroupIdAndUser(groupId, userId, pageable);
        List<SplitActivity> activities = entities.getContent().stream()
            .map(this::toDomain)
            .collect(Collectors.toList());
        return new PageImpl<>(activities, pageable, entities.getTotalElements());
    }

    @Override
    public List<SplitActivity> findByGroupId(Long groupId) {
        List<SplitActivityEntity> entities = jpaRepository.findByGroupIdOrderByCreatedAtDesc(groupId);
        return entities.stream()
            .map(this::toDomain)
            .collect(Collectors.toList());
    }

    @Override
    public Page<SplitActivity> findBySplitExpenseIdAndUser(Long splitExpenseId, Long userId, Pageable pageable) {
        Page<SplitActivityEntity> entities = jpaRepository.findBySplitExpenseIdAndUser(splitExpenseId, userId, pageable);
        List<SplitActivity> activities = entities.getContent().stream()
            .map(this::toDomain)
            .collect(Collectors.toList());
        return new PageImpl<>(activities, pageable, entities.getTotalElements());
    }

    @Override
    public List<SplitActivity> findBySplitExpenseId(Long splitExpenseId) {
        List<SplitActivityEntity> entities = jpaRepository.findBySplitExpenseIdOrderByCreatedAtDesc(splitExpenseId);
        return entities.stream()
            .map(this::toDomain)
            .collect(Collectors.toList());
    }

    @Override
    public Page<SplitActivity> findFriendActivitiesForUser(Long userId, Long friendId, Pageable pageable) {
        Page<SplitActivityEntity> entities = jpaRepository.findFriendActivitiesForUser(userId, friendId, pageable);
        List<SplitActivity> activities = entities.getContent().stream()
            .map(this::toDomain)
            .collect(Collectors.toList());
        return new PageImpl<>(activities, pageable, entities.getTotalElements());
    }

    @Override
    public List<SplitActivity> findRecentActivitiesForUser(Long userId, int limit) {
        List<SplitActivityEntity> entities = jpaRepository.findRecentActivitiesForUser(userId, limit);
        return entities.stream()
            .map(this::toDomain)
            .collect(Collectors.toList());
    }

    @Override
    public void deleteById(Long id) {
        jpaRepository.deleteById(id);
    }

    @Override
    public void deleteByGroupId(Long groupId) {
        jpaRepository.deleteByGroupId(groupId);
    }

    @Override
    public void deleteBySplitExpenseId(Long splitExpenseId) {
        jpaRepository.deleteBySplitExpenseId(splitExpenseId);
    }

    private SplitActivityEntity toEntity(SplitActivity activity) {
        SplitActivityEntity entity = new SplitActivityEntity();
        entity.setId(activity.getId());
        entity.setUserId(activity.getUserId());
        entity.setActivityType(activity.getActivityType());
        entity.setActivityData(activity.getActivityData());
        entity.setRelatedUserId(activity.getRelatedUserId());
        entity.setGroupId(activity.getGroupId());
        entity.setSplitExpenseId(activity.getSplitExpenseId());
        entity.setCreatedAt(activity.getCreatedAt());
        return entity;
    }

    private SplitActivity toDomain(SplitActivityEntity entity) {
        SplitActivity activity = new SplitActivity(
            entity.getUserId(),
            entity.getActivityType(),
            entity.getActivityData(),
            entity.getRelatedUserId(),
            entity.getGroupId(),
            entity.getSplitExpenseId()
        );
        activity.setId(entity.getId());
        activity.setCreatedAt(entity.getCreatedAt());
        return activity;
    }
}