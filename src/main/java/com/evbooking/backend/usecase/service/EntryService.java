package com.evbooking.backend.usecase.service;

import com.evbooking.backend.domain.model.Entry;
import com.evbooking.backend.domain.model.EntryType;
import com.evbooking.backend.domain.repository.EntryRepository;
import com.evbooking.backend.domain.service.EntryDomainService;
import com.evbooking.backend.presentation.dto.DashboardResponse;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Service
public class EntryService {

    private final EntryRepository entryRepository;
    private final BookService bookService;

    public EntryService(EntryRepository entryRepository, BookService bookService) {
        this.entryRepository = entryRepository;
        this.bookService = bookService;
    }

    public Entry createEntry(Long bookId, EntryType type, String name, BigDecimal amount,
                           String currency, LocalDateTime dateTime, String userId) {
        if (bookId == null) {
            throw new RuntimeException("Book ID is required");
        }

        if (type == null) {
            throw new RuntimeException("Entry type is required");
        }

        if (name == null || name.trim().isEmpty()) {
            throw new RuntimeException("Entry name is required");
        }

        if (amount == null || amount.compareTo(BigDecimal.ZERO) <= 0) {
            throw new RuntimeException("Amount must be greater than zero");
        }

        if (dateTime == null) {
            throw new RuntimeException("Date time is required");
        }

        if (userId == null) {
            throw new RuntimeException("User ID is required");
        }

        if (!bookService.verifyBookOwnership(bookId, userId)) {
            throw new RuntimeException("You can only add entries to your own books");
        }

        Entry entry = new Entry(bookId, type, name.trim(), amount, currency, dateTime);
        return entryRepository.save(entry);
    }

    public Entry updateEntry(Long entryId, EntryType type, String name, BigDecimal amount,
                           String currency, LocalDateTime dateTime, String userId) {
        if (entryId == null) {
            throw new RuntimeException("Entry ID is required");
        }

        Optional<Entry> entryOpt = entryRepository.findById(entryId);
        if (entryOpt.isEmpty()) {
            throw new RuntimeException("Entry not found");
        }

        Entry entry = entryOpt.get();

        if (!bookService.verifyBookOwnership(entry.getBookId(), userId)) {
            throw new RuntimeException("You can only edit entries in your own books");
        }

        if (type != null) {
            entry.setType(type);
        }

        if (name != null && !name.trim().isEmpty()) {
            entry.setName(name.trim());
        }

        if (amount != null && amount.compareTo(BigDecimal.ZERO) > 0) {
            entry.setAmount(amount);
        }

        if (currency != null && !currency.trim().isEmpty()) {
            entry.setCurrency(currency.trim());
        }

        if (dateTime != null) {
            entry.setDateTime(dateTime);
        }

        return entryRepository.save(entry);
    }

    public Page<Entry> getEntriesByBookId(Long bookId, String type, String userId, Pageable pageable) {
        if (bookId == null) {
            throw new RuntimeException("Book ID is required");
        }

        if (userId == null) {
            throw new RuntimeException("User ID is required");
        }

        if (!bookService.verifyBookOwnership(bookId, userId)) {
            throw new RuntimeException("You can only view entries from your own books");
        }

        // If type filter is provided, filter by type
        if (type != null && !type.trim().isEmpty()) {
            try {
                EntryType entryType = EntryType.valueOf(type.toUpperCase());
                return entryRepository.findByBookIdAndType(bookId, entryType, pageable);
            } catch (IllegalArgumentException e) {
                throw new RuntimeException("Invalid entry type. Must be INCOME or EXPENSE");
            }
        }

        return entryRepository.findByBookId(bookId, pageable);
    }

    public Page<Entry> searchEntries(Long bookId, String query, String userId, Pageable pageable) {
        if (bookId == null) {
            throw new RuntimeException("Book ID is required");
        }

        if (userId == null) {
            throw new RuntimeException("User ID is required");
        }

        if (!bookService.verifyBookOwnership(bookId, userId)) {
            throw new RuntimeException("You can only search entries from your own books");
        }

        // If no query provided, return all entries
        if (query == null || query.trim().isEmpty()) {
            return entryRepository.findByBookId(bookId, pageable);
        }

        return entryRepository.searchByBookIdAndQuery(bookId, query.trim(), pageable);
    }

    public Optional<Entry> getEntryById(Long entryId, String userId) {
        if (entryId == null) {
            throw new RuntimeException("Entry ID is required");
        }

        if (userId == null) {
            throw new RuntimeException("User ID is required");
        }

        Optional<Entry> entryOpt = entryRepository.findById(entryId);
        if (entryOpt.isEmpty()) {
            return Optional.empty();
        }

        Entry entry = entryOpt.get();
        if (!bookService.verifyBookOwnership(entry.getBookId(), userId)) {
            throw new RuntimeException("You can only view entries from your own books");
        }

        return entryOpt;
    }

    public void deleteEntry(Long entryId, String userId) {
        if (entryId == null) {
            throw new RuntimeException("Entry ID is required");
        }

        if (userId == null) {
            throw new RuntimeException("User ID is required");
        }

        Optional<Entry> entryOpt = entryRepository.findById(entryId);
        if (entryOpt.isEmpty()) {
            throw new RuntimeException("Entry not found");
        }

        Entry entry = entryOpt.get();
        if (!bookService.verifyBookOwnership(entry.getBookId(), userId)) {
            throw new RuntimeException("You can only delete entries from your own books");
        }

        entryRepository.deleteById(entryId);
    }

    public List<Entry> getEntriesByBookIdAndDateRange(Long bookId, LocalDateTime startDate,
                                                     LocalDateTime endDate, String userId) {
        if (bookId == null) {
            throw new RuntimeException("Book ID is required");
        }

        if (startDate == null) {
            throw new RuntimeException("Start date is required");
        }

        if (endDate == null) {
            throw new RuntimeException("End date is required");
        }

        if (userId == null) {
            throw new RuntimeException("User ID is required");
        }

        if (!bookService.verifyBookOwnership(bookId, userId)) {
            throw new RuntimeException("You can only view entries from your own books");
        }

        return entryRepository.findByBookIdAndDateTimeBetween(bookId, startDate, endDate);
    }

    public Page<Entry> getRecentEntries(String userId, String query, Pageable pageable) {
        if (userId == null) {
            throw new RuntimeException("User ID is required");
        }

        // If query is provided, search recent entries
        if (query != null && !query.trim().isEmpty()) {
            return entryRepository.searchRecentEntriesByUserId(userId, query.trim(), pageable);
        }

        return entryRepository.findRecentEntriesByUserId(userId, pageable);
    }

    public DashboardResponse getDashboard(String userId, BigDecimal goalAmount) {
        if (userId == null) {
            throw new RuntimeException("User ID is required");
        }

        // Get current date for this month calculations
        LocalDateTime now = LocalDateTime.now();
        LocalDateTime startOfMonth = now.withDayOfMonth(1).withHour(0).withMinute(0).withSecond(0).withNano(0);
        LocalDateTime endOfMonth = now.withDayOfMonth(now.toLocalDate().lengthOfMonth())
                .withHour(23).withMinute(59).withSecond(59).withNano(999999999);

        // Get overall totals
        BigDecimal totalExpense = entryRepository.getTotalExpensesByUserId(userId);
        BigDecimal totalIncome = entryRepository.getTotalIncomeByUserId(userId);
        BigDecimal totalSavings = totalIncome.subtract(totalExpense);

        // Get this month totals
        BigDecimal thisMonthExpense = entryRepository.getTotalExpensesByUserIdAndDateRange(userId, startOfMonth, endOfMonth);
        BigDecimal thisMonthIncome = entryRepository.getTotalIncomeByUserIdAndDateRange(userId, startOfMonth, endOfMonth);
        BigDecimal thisMonthSavings = thisMonthIncome.subtract(thisMonthExpense);

        // Calculate goal progress
        BigDecimal goalProgress = BigDecimal.ZERO;
        BigDecimal goalReachedAmount = BigDecimal.ZERO;

        if (goalAmount != null && goalAmount.compareTo(BigDecimal.ZERO) > 0) {
            goalReachedAmount = totalSavings;
            goalProgress = goalReachedAmount.divide(goalAmount, 4, BigDecimal.ROUND_HALF_UP)
                    .multiply(new BigDecimal("100"));

            // Cap progress at 100%
            if (goalProgress.compareTo(new BigDecimal("100")) > 0) {
                goalProgress = new BigDecimal("100");
            }
        }

        return new DashboardResponse(
                totalExpense,
                totalIncome,
                totalSavings,
                thisMonthExpense,
                thisMonthIncome,
                thisMonthSavings,
                goalAmount,
                goalProgress,
                goalReachedAmount
        );
    }
}