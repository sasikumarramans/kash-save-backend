package com.evbooking.backend.infrastructure.repository;

import com.evbooking.backend.domain.model.EntryType;
import com.evbooking.backend.infrastructure.entity.EntryEntity;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface JpaEntryRepository extends JpaRepository<EntryEntity, Long> {
    Page<EntryEntity> findByBookIdOrderByDateTimeDesc(Long bookId, Pageable pageable);
    List<EntryEntity> findByBookIdOrderByDateTimeDesc(Long bookId);
    List<EntryEntity> findByBookIdAndDateTimeBetweenOrderByDateTimeDesc(
        Long bookId, LocalDateTime startDate, LocalDateTime endDate);

    @Query("SELECT COALESCE(SUM(e.amount), 0) FROM EntryEntity e WHERE e.bookId = :bookId AND e.type = :type")
    BigDecimal getTotalAmountByBookIdAndType(@Param("bookId") Long bookId, @Param("type") EntryType type);

    @Query("SELECT COALESCE(SUM(e.amount), 0) FROM EntryEntity e " +
           "WHERE e.bookId = :bookId AND e.type = :type AND e.dateTime BETWEEN :startDate AND :endDate")
    BigDecimal getTotalAmountByBookIdAndTypeAndDateRange(
        @Param("bookId") Long bookId,
        @Param("type") EntryType type,
        @Param("startDate") LocalDateTime startDate,
        @Param("endDate") LocalDateTime endDate);

    @Query("SELECT MAX(e.dateTime) FROM EntryEntity e WHERE e.bookId = :bookId")
    LocalDateTime findLatestEntryDateTimeByBookId(@Param("bookId") Long bookId);

    // User-wide queries (across all user's books)
    @Query("SELECT COALESCE(SUM(e.amount), 0) FROM EntryEntity e " +
           "JOIN BookEntity b ON e.bookId = b.id " +
           "WHERE b.userId = :userId AND e.type = :type AND e.dateTime BETWEEN :startDate AND :endDate")
    BigDecimal getTotalAmountByUserIdAndTypeAndDateRange(
        @Param("userId") Long userId,
        @Param("type") EntryType type,
        @Param("startDate") LocalDateTime startDate,
        @Param("endDate") LocalDateTime endDate);

    @Query("SELECT e FROM EntryEntity e " +
           "JOIN BookEntity b ON e.bookId = b.id " +
           "WHERE b.userId = :userId AND e.dateTime BETWEEN :startDate AND :endDate " +
           "ORDER BY e.dateTime DESC")
    List<EntryEntity> findByUserIdAndDateTimeBetweenOrderByDateTimeDesc(
        @Param("userId") Long userId,
        @Param("startDate") LocalDateTime startDate,
        @Param("endDate") LocalDateTime endDate);

    // Overall user totals (all time)
    @Query("SELECT COALESCE(SUM(e.amount), 0) FROM EntryEntity e " +
           "JOIN BookEntity b ON e.bookId = b.id " +
           "WHERE b.userId = :userId AND e.type = :type")
    BigDecimal getTotalAmountByUserIdAndType(
        @Param("userId") Long userId,
        @Param("type") EntryType type);
}