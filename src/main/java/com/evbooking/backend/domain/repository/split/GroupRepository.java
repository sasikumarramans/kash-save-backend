package com.evbooking.backend.domain.repository.split;

import com.evbooking.backend.domain.model.split.Group;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.List;
import java.util.Optional;

public interface GroupRepository {
    Group save(Group group);
    Optional<Group> findById(Long id);
    List<Group> findByAdminUserId(Long adminUserId);
    Page<Group> findByAdminUserId(Long adminUserId, Pageable pageable);
    List<Group> findGroupsByMemberId(Long userId);
    Page<Group> findGroupsByMemberId(Long userId, Pageable pageable);
    void deleteById(Long id);
    boolean existsByIdAndAdminUserId(Long id, Long adminUserId);
}