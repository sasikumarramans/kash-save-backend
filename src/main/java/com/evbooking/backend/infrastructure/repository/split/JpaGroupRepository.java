package com.evbooking.backend.infrastructure.repository.split;

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
public interface JpaGroupRepository extends JpaRepository<GroupEntity, Long> {

    @Query("SELECT g FROM GroupEntity g JOIN GroupMemberEntity gm ON g.id = gm.groupId WHERE gm.userId = :userId")
    Page<GroupEntity> findByUserId(@Param("userId") Long userId, Pageable pageable);

    @Query("SELECT g FROM GroupEntity g JOIN GroupMemberEntity gm ON g.id = gm.groupId WHERE g.id = :groupId AND gm.userId = :userId")
    Optional<GroupEntity> findByIdAndUserId(@Param("groupId") Long groupId, @Param("userId") Long userId);

    List<GroupEntity> findByAdminUserId(Long adminUserId);
}