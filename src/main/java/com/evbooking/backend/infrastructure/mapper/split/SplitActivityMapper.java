package com.evbooking.backend.infrastructure.mapper.split;

import com.evbooking.backend.domain.model.split.SplitActivity;
import com.evbooking.backend.infrastructure.entity.split.SplitActivityEntity;
import org.springframework.stereotype.Component;

@Component
public class SplitActivityMapper {

    public SplitActivity toDomain(SplitActivityEntity entity) {
        if (entity == null) {
            return null;
        }

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

    public SplitActivityEntity toEntity(SplitActivity domain) {
        if (domain == null) {
            return null;
        }

        SplitActivityEntity entity = new SplitActivityEntity();
        entity.setId(domain.getId());
        entity.setUserId(domain.getUserId());
        entity.setActivityType(domain.getActivityType());
        entity.setActivityData(domain.getActivityData());
        entity.setRelatedUserId(domain.getRelatedUserId());
        entity.setGroupId(domain.getGroupId());
        entity.setSplitExpenseId(domain.getSplitExpenseId());
        entity.setCreatedAt(domain.getCreatedAt());

        return entity;
    }
}