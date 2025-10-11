package com.evbooking.backend.domain.repository.split;

import com.evbooking.backend.infrastructure.entity.split.GroupMemberEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface GroupMemberRepository extends JpaRepository<GroupMemberEntity, Long> {

    List<GroupMemberEntity> findByGroupId(Long groupId);

    List<GroupMemberEntity> findByUserId(String userId);

    @Query("SELECT gm FROM GroupMemberEntity gm WHERE gm.groupId = :groupId AND gm.userId = :userId")
    Optional<GroupMemberEntity> findByGroupIdAndUserId(@Param("groupId") Long groupId, @Param("userId") String userId);

    List<GroupMemberEntity> findByGroupIdAndIsAdminTrue(Long groupId);

    @Query("SELECT gm FROM GroupMemberEntity gm WHERE gm.groupId = :groupId AND gm.isAdmin = true")
    List<GroupMemberEntity> findAdminsByGroupId(@Param("groupId") Long groupId);

    void deleteByGroupIdAndUserId(Long groupId, String userId);

    void deleteByGroupId(Long groupId);

    @Query("SELECT gm.userId FROM GroupMemberEntity gm WHERE gm.groupId = :groupId")
    List<String> findUserIdsByGroupId(@Param("groupId") Long groupId);

    boolean existsByGroupIdAndUserId(Long groupId, String userId);

    long countByGroupId(Long groupId);
}