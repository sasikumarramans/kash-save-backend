package com.evbooking.backend.domain.repository.split;

import com.evbooking.backend.infrastructure.entity.split.GroupEntity;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface GroupRepository extends JpaRepository<GroupEntity, Long> {

    @Query("SELECT g FROM GroupEntity g JOIN GroupMemberEntity gm ON g.id = gm.groupId WHERE gm.userId = :userId")
    Page<GroupEntity> findGroupsByMemberId(@Param("userId") String userId, Pageable pageable);

    @Query("SELECT g FROM GroupEntity g JOIN GroupMemberEntity gm ON g.id = gm.groupId WHERE g.id = :groupId AND gm.userId = :userId")
    Optional<GroupEntity> findByIdAndUserId(@Param("groupId") String userId);

    List<GroupEntity> findByAdminUserId(String adminUserId);

    boolean existsByIdAndAdminUserId(Long id, String adminUserId);
}