package com.evbooking.backend.infrastructure.repository;

import com.evbooking.backend.domain.model.EntryType;
import com.evbooking.backend.domain.repository.EntryRepository;
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
    Page<EntryEntity> findByBookIdAndTypeOrderByDateTimeDesc(Long bookId, EntryType type, Pageable pageable);
    List<EntryEntity> findByBookIdOrderByDateTimeDesc(Long bookId);
    List<EntryEntity> findByBookIdAndDateTimeBetweenOrderByDateTimeDesc(
        Long bookId, LocalDateTime startDate, LocalDateTime endDate);

    @Query(value = "SELECT * FROM entries e WHERE e.book_id = :bookId AND " +
           "(LOWER(e.name) LIKE LOWER(CONCAT('%', :query, '%')) OR " +
           "CAST(e.amount AS VARCHAR) LIKE CONCAT('%', :query, '%') OR " +
           "CAST(EXTRACT(YEAR FROM e.date_time) AS VARCHAR) LIKE CONCAT('%', :query, '%') OR " +
           "CAST(EXTRACT(MONTH FROM e.date_time) AS VARCHAR) LIKE CONCAT('%', :query, '%') OR " +
           "CAST(EXTRACT(DAY FROM e.date_time) AS VARCHAR) LIKE CONCAT('%', :query, '%') OR " +
           "TO_CHAR(e.date_time, 'YYYY-MM-DD') LIKE CONCAT('%', :query, '%')) " +
           "ORDER BY e.date_time DESC",
           nativeQuery = true)
    Page<EntryEntity> searchByBookIdAndQuery(@Param("bookId") Long bookId, @Param("query") String query, Pageable pageable);

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
        @Param("userId") String userId,
        @Param("type") EntryType type,
        @Param("startDate") LocalDateTime startDate,
        @Param("endDate") LocalDateTime endDate);

    @Query("SELECT e FROM EntryEntity e " +
           "JOIN BookEntity b ON e.bookId = b.id " +
           "WHERE b.userId = :userId AND e.dateTime BETWEEN :startDate AND :endDate " +
           "ORDER BY e.dateTime DESC")
    List<EntryEntity> findByUserIdAndDateTimeBetweenOrderByDateTimeDesc(
        @Param("userId") String userId,
        @Param("startDate") LocalDateTime startDate,
        @Param("endDate") LocalDateTime endDate);

    @Query("SELECT e FROM EntryEntity e " +
           "JOIN BookEntity b ON e.bookId = b.id " +
           "WHERE b.userId = :userId " +
           "ORDER BY e.dateTime DESC")
    Page<EntryEntity> findRecentEntriesByUserIdOrderByDateTimeDesc(@Param("userId") String userId, Pageable pageable);

    @Query(value = "SELECT e.* FROM entries e " +
           "JOIN books b ON e.book_id = b.id " +
           "WHERE b.user_id = :userId AND " +
           "(LOWER(e.name) LIKE LOWER(CONCAT('%', :query, '%')) OR " +
           "CAST(e.amount AS VARCHAR) LIKE CONCAT('%', :query, '%')) " +
           "ORDER BY e.date_time DESC",
           nativeQuery = true)
    Page<EntryEntity> searchRecentEntriesByUserIdOrderByDateTimeDesc(@Param("userId") String userId, @Param("query") String query, Pageable pageable);

    // Overall user totals (all time)
    @Query("SELECT COALESCE(SUM(e.amount), 0) FROM EntryEntity e " +
           "JOIN BookEntity b ON e.bookId = b.id " +
           "WHERE b.userId = :userId AND e.type = :type")
    BigDecimal getTotalAmountByUserIdAndType(
        @Param("userId") String userId,
        @Param("type") EntryType type);

    // Category aggregations for user
    @Query(value = "SELECT e.name AS name, SUM(e.amount) AS totalAmount, COUNT(e.id) AS count " +
           "FROM entries e " +
           "JOIN books b ON e.book_id = b.id " +
           "WHERE b.user_id = :userId AND e.type = :type AND e.date_time BETWEEN :startDate AND :endDate " +
           "GROUP BY e.name " +
           "ORDER BY totalAmount DESC",
           nativeQuery = true)
    List<EntryRepository.CategoryData> getCategoryDataByUserIdAndDateRange(
        @Param("userId") String userId,
        @Param("type") EntryType type,
        @Param("startDate") LocalDateTime startDate,
        @Param("endDate") LocalDateTime endDate);

    // Category aggregations for book
    @Query(value = "SELECT e.name AS name, SUM(e.amount) AS totalAmount, COUNT(e.id) AS count " +
           "FROM entries e " +
           "WHERE e.book_id = :bookId AND e.type = :type AND e.date_time BETWEEN :startDate AND :endDate " +
           "GROUP BY e.name " +
           "ORDER BY totalAmount DESC",
           nativeQuery = true)
    List<EntryRepository.CategoryData> getCategoryDataByBookIdAndDateRange(
        @Param("bookId") Long bookId,
        @Param("type") EntryType type,
        @Param("startDate") LocalDateTime startDate,
        @Param("endDate") LocalDateTime endDate);
}