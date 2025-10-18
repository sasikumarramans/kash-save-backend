package com.evbooking.backend.infrastructure.mapper.split;

import com.evbooking.backend.domain.model.split.GroupDeletionHistory;
import com.evbooking.backend.infrastructure.entity.split.GroupDeletionHistoryEntity;
import org.springframework.stereotype.Component;

@Component
public class GroupDeletionHistoryMapper {

    public GroupDeletionHistoryEntity toEntity(GroupDeletionHistory domain) {
        if (domain == null) return null;

        GroupDeletionHistoryEntity entity = new GroupDeletionHistoryEntity();
        entity.setId(domain.getId());
        entity.setGroupId(domain.getGroupId());
        entity.setGroupName(domain.getGroupName());
        entity.setGroupDescription(domain.getGroupDescription());
        entity.setDeletedByUserId(domain.getDeletedByUserId());
        entity.setDeletedByUsername(domain.getDeletedByUsername());
        entity.setDeletedAt(domain.getDeletedAt());
        entity.setMemberCount(domain.getMemberCount());
        entity.setReason(domain.getReason());
        return entity;
    }

    public GroupDeletionHistory toDomain(GroupDeletionHistoryEntity entity) {
        if (entity == null) return null;

        GroupDeletionHistory domain = new GroupDeletionHistory();
        domain.setId(entity.getId());
        domain.setGroupId(entity.getGroupId());
        domain.setGroupName(entity.getGroupName());
        domain.setGroupDescription(entity.getGroupDescription());
        domain.setDeletedByUserId(entity.getDeletedByUserId());
        domain.setDeletedByUsername(entity.getDeletedByUsername());
        domain.setDeletedAt(entity.getDeletedAt());
        domain.setMemberCount(entity.getMemberCount());
        domain.setReason(entity.getReason());
        return domain;
    }
}
