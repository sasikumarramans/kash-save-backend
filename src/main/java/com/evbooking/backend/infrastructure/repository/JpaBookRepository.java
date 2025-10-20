package com.evbooking.backend.infrastructure.repository;

import com.evbooking.backend.infrastructure.entity.BookEntity;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface JpaBookRepository extends JpaRepository<BookEntity, Long> {
    List<BookEntity> findByUserIdOrderByCreatedAtDesc(String userId);
    Page<BookEntity> findByUserIdOrderByCreatedAtDesc(String userId, Pageable pageable);

    @Query("SELECT b FROM BookEntity b WHERE b.userId = :userId AND " +
           "LOWER(b.name) LIKE LOWER(CONCAT('%', :name, '%')) " +
           "ORDER BY b.createdAt DESC")
    Page<BookEntity> searchByUserIdAndName(@Param("userId") String userId, @Param("name") String name, Pageable pageable);
}