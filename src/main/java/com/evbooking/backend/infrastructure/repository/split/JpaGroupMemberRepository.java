package com.evbooking.backend.infrastructure.repository.split;

import com.evbooking.backend.infrastructure.entity.split.GroupMemberEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface JpaGroupMemberRepository extends JpaRepository<GroupMemberEntity, Long> {

    List<GroupMemberEntity> findByGroupId(Long groupId);

    List<GroupMemberEntity> findByUserId(Long userId);

    @Query("SELECT gm FROM GroupMemberEntity gm WHERE gm.groupId = :groupId AND gm.userId = :userId")
    Optional<GroupMemberEntity> findByGroupIdAndUserId(@Param("groupId") Long groupId, @Param("userId") Long userId);

    List<GroupMemberEntity> findByGroupIdAndIsAdminTrue(Long groupId);

    void deleteByGroupIdAndUserId(Long groupId, Long userId);

    void deleteByGroupId(Long groupId);

    @Query("SELECT gm.userId FROM GroupMemberEntity gm WHERE gm.groupId = :groupId")
    List<Long> findUserIdsByGroupId(@Param("groupId") Long groupId);
}