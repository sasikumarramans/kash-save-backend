package com.evbooking.backend.infrastructure.mapper.split;

import com.evbooking.backend.domain.model.split.GroupMember;
import com.evbooking.backend.infrastructure.entity.split.GroupMemberEntity;
import org.springframework.stereotype.Component;

@Component
public class GroupMemberMapper {

    public GroupMember toDomain(GroupMemberEntity entity) {
        if (entity == null) {
            return null;
        }

        GroupMember member = new GroupMember(
                entity.getGroupId(),
                entity.getUserId(),
                entity.isAdmin()
        );

        member.setId(entity.getId());
        member.setJoinedAt(entity.getJoinedAt());

        return member;
    }

    public GroupMemberEntity toEntity(GroupMember domain) {
        if (domain == null) {
            return null;
        }

        GroupMemberEntity entity = new GroupMemberEntity(
                domain.getGroupId(),
                domain.getUserId(),
                domain.isAdmin()
        );

        entity.setId(domain.getId());
        entity.setJoinedAt(domain.getJoinedAt());

        return entity;
    }
}