package com.evbooking.backend.infrastructure.mapper.split;

import com.evbooking.backend.domain.model.split.Group;
import com.evbooking.backend.infrastructure.entity.split.GroupEntity;
import org.springframework.stereotype.Component;

@Component
public class GroupMapper {

    public Group toDomain(GroupEntity entity) {
        if (entity == null) {
            return null;
        }

        Group group = new Group(
                entity.getName(),
                entity.getDescription(),
                entity.getAdminUserId(),
                entity.getCurrency()
        );

        group.setId(entity.getId());
        group.setCreatedAt(entity.getCreatedAt());
        group.setUpdatedAt(entity.getUpdatedAt());

        return group;
    }

    public GroupEntity toEntity(Group domain) {
        if (domain == null) {
            return null;
        }

        GroupEntity entity = new GroupEntity(
                domain.getName(),
                domain.getDescription(),
                domain.getAdminUserId(),
                domain.getCurrency()
        );

        entity.setId(domain.getId());
        entity.setCreatedAt(domain.getCreatedAt());
        entity.setUpdatedAt(domain.getUpdatedAt());

        return entity;
    }
}