package com.evbooking.backend.domain.repository.split;

import com.evbooking.backend.infrastructure.entity.split.GroupDeletionHistoryEntity;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface GroupDeletionHistoryRepository extends JpaRepository<GroupDeletionHistoryEntity, Long> {

    // Find deletion history by group ID
    List<GroupDeletionHistoryEntity> findByGroupId(Long groupId);

    // Find deletion history by user who deleted
    Page<GroupDeletionHistoryEntity> findByDeletedByUserId(String deletedByUserId, Pageable pageable);

    // Check if a group was ever deleted
    boolean existsByGroupId(Long groupId);
}
