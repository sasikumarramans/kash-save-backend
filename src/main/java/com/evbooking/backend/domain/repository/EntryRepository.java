package com.evbooking.backend.domain.repository;

import com.evbooking.backend.domain.model.Entry;
import com.evbooking.backend.domain.model.EntryType;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

public interface EntryRepository {
    Optional<Entry> findById(Long id);
    Page<Entry> findByBookId(Long bookId, Pageable pageable);
    List<Entry> findByBookId(Long bookId);
    List<Entry> findByBookIdAndDateTimeBetween(Long bookId, LocalDateTime startDate, LocalDateTime endDate);
    Entry save(Entry entry);
    void deleteById(Long id);
    boolean existsById(Long id);

    BigDecimal getTotalExpensesByBookId(Long bookId);
    BigDecimal getTotalIncomeByBookId(Long bookId);
    BigDecimal getTotalExpensesByBookIdAndDateRange(Long bookId, LocalDateTime startDate, LocalDateTime endDate);
    BigDecimal getTotalIncomeByBookIdAndDateRange(Long bookId, LocalDateTime startDate, LocalDateTime endDate);
    Optional<LocalDateTime> getLatestEntryDateTimeByBookId(Long bookId);

    // User-wide reports (across all user's books)
    BigDecimal getTotalExpensesByUserIdAndDateRange(Long userId, LocalDateTime startDate, LocalDateTime endDate);
    BigDecimal getTotalIncomeByUserIdAndDateRange(Long userId, LocalDateTime startDate, LocalDateTime endDate);
    List<Entry> findByUserIdAndDateTimeBetween(Long userId, LocalDateTime startDate, LocalDateTime endDate);

    // Overall user totals (all time)
    BigDecimal getTotalExpensesByUserId(Long userId);
    BigDecimal getTotalIncomeByUserId(Long userId);
}