package com.evbooking.backend.infrastructure.repository;

import com.evbooking.backend.infrastructure.entity.BookEntity;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface JpaBookRepository extends JpaRepository<BookEntity, Long> {
    List<BookEntity> findByUserIdOrderByCreatedAtDesc(Long userId);
    Page<BookEntity> findByUserIdOrderByCreatedAtDesc(Long userId, Pageable pageable);
}