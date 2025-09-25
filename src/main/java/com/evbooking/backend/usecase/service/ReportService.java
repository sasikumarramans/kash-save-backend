package com.evbooking.backend.usecase.service;

import com.evbooking.backend.domain.repository.EntryRepository;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Service
public class ReportService {

    private final EntryRepository entryRepository;
    private final BookService bookService;

    public ReportService(EntryRepository entryRepository, BookService bookService) {
        this.entryRepository = entryRepository;
        this.bookService = bookService;
    }

    public OverallReport getOverallReport(Long bookId, Long userId) {
        if (bookId == null) {
            throw new RuntimeException("Book ID is required");
        }

        if (userId == null) {
            throw new RuntimeException("User ID is required");
        }

        if (!bookService.verifyBookOwnership(bookId, userId)) {
            throw new RuntimeException("You can only view reports for your own books");
        }

        BigDecimal totalExpense = entryRepository.getTotalExpensesByBookId(bookId);
        BigDecimal totalIncome = entryRepository.getTotalIncomeByBookId(bookId);
        BigDecimal balance = totalIncome.subtract(totalExpense);

        return new OverallReport(totalExpense, totalIncome, balance);
    }

    public DateRangeReport getDateRangeReport(Long bookId, LocalDateTime startDate, LocalDateTime endDate, Long userId) {
        if (bookId == null) {
            throw new RuntimeException("Book ID is required");
        }

        if (startDate == null) {
            throw new RuntimeException("Start date is required");
        }

        if (endDate == null) {
            throw new RuntimeException("End date is required");
        }

        if (startDate.isAfter(endDate)) {
            throw new RuntimeException("Start date cannot be after end date");
        }

        if (userId == null) {
            throw new RuntimeException("User ID is required");
        }

        if (!bookService.verifyBookOwnership(bookId, userId)) {
            throw new RuntimeException("You can only view reports for your own books");
        }

        BigDecimal totalExpense = entryRepository.getTotalExpensesByBookIdAndDateRange(bookId, startDate, endDate);
        BigDecimal totalIncome = entryRepository.getTotalIncomeByBookIdAndDateRange(bookId, startDate, endDate);
        BigDecimal balance = totalIncome.subtract(totalExpense);

        return new DateRangeReport(totalExpense, totalIncome, balance, startDate, endDate);
    }

    public UserDateRangeReport getUserDateRangeReport(Long userId, LocalDateTime startDate, LocalDateTime endDate) {
        if (userId == null) {
            throw new RuntimeException("User ID is required");
        }

        if (startDate == null) {
            throw new RuntimeException("Start date is required");
        }

        if (endDate == null) {
            throw new RuntimeException("End date is required");
        }

        if (startDate.isAfter(endDate)) {
            throw new RuntimeException("Start date cannot be after end date");
        }

        BigDecimal totalExpense = entryRepository.getTotalExpensesByUserIdAndDateRange(userId, startDate, endDate);
        BigDecimal totalIncome = entryRepository.getTotalIncomeByUserIdAndDateRange(userId, startDate, endDate);
        BigDecimal balance = totalIncome.subtract(totalExpense);

        return new UserDateRangeReport(totalExpense, totalIncome, balance, startDate, endDate, userId);
    }

    public UserOverallReport getUserOverallReport(Long userId) {
        if (userId == null) {
            throw new RuntimeException("User ID is required");
        }

        // Overall totals (all time)
        BigDecimal totalExpenseAllTime = entryRepository.getTotalExpensesByUserId(userId);
        BigDecimal totalIncomeAllTime = entryRepository.getTotalIncomeByUserId(userId);
        BigDecimal balanceAllTime = totalIncomeAllTime.subtract(totalExpenseAllTime);

        // Current month totals
        LocalDateTime now = LocalDateTime.now();
        LocalDateTime startOfMonth = now.withDayOfMonth(1).withHour(0).withMinute(0).withSecond(0).withNano(0);
        LocalDateTime endOfMonth = now.withDayOfMonth(now.toLocalDate().lengthOfMonth()).withHour(23).withMinute(59).withSecond(59).withNano(999999999);

        BigDecimal currentMonthExpense = entryRepository.getTotalExpensesByUserIdAndDateRange(userId, startOfMonth, endOfMonth);
        BigDecimal currentMonthIncome = entryRepository.getTotalIncomeByUserIdAndDateRange(userId, startOfMonth, endOfMonth);
        BigDecimal currentMonthSavings = currentMonthIncome.subtract(currentMonthExpense);

        return new UserOverallReport(
            totalExpenseAllTime,
            totalIncomeAllTime,
            balanceAllTime,
            currentMonthSavings,
            userId
        );
    }

    public static class OverallReport {
        private final BigDecimal totalExpense;
        private final BigDecimal totalIncome;
        private final BigDecimal balance;

        public OverallReport(BigDecimal totalExpense, BigDecimal totalIncome, BigDecimal balance) {
            this.totalExpense = totalExpense;
            this.totalIncome = totalIncome;
            this.balance = balance;
        }

        public BigDecimal getTotalExpense() { return totalExpense; }
        public BigDecimal getTotalIncome() { return totalIncome; }
        public BigDecimal getBalance() { return balance; }
    }

    public static class DateRangeReport {
        private final BigDecimal totalExpense;
        private final BigDecimal totalIncome;
        private final BigDecimal balance;
        private final LocalDateTime startDate;
        private final LocalDateTime endDate;

        public DateRangeReport(BigDecimal totalExpense, BigDecimal totalIncome, BigDecimal balance,
                              LocalDateTime startDate, LocalDateTime endDate) {
            this.totalExpense = totalExpense;
            this.totalIncome = totalIncome;
            this.balance = balance;
            this.startDate = startDate;
            this.endDate = endDate;
        }

        public BigDecimal getTotalExpense() { return totalExpense; }
        public BigDecimal getTotalIncome() { return totalIncome; }
        public BigDecimal getBalance() { return balance; }
        public LocalDateTime getStartDate() { return startDate; }
        public LocalDateTime getEndDate() { return endDate; }
    }

    public static class UserDateRangeReport {
        private final BigDecimal totalExpense;
        private final BigDecimal totalIncome;
        private final BigDecimal balance;
        private final LocalDateTime startDate;
        private final LocalDateTime endDate;
        private final Long userId;

        public UserDateRangeReport(BigDecimal totalExpense, BigDecimal totalIncome, BigDecimal balance,
                                  LocalDateTime startDate, LocalDateTime endDate, Long userId) {
            this.totalExpense = totalExpense;
            this.totalIncome = totalIncome;
            this.balance = balance;
            this.startDate = startDate;
            this.endDate = endDate;
            this.userId = userId;
        }

        public BigDecimal getTotalExpense() { return totalExpense; }
        public BigDecimal getTotalIncome() { return totalIncome; }
        public BigDecimal getBalance() { return balance; }
        public LocalDateTime getStartDate() { return startDate; }
        public LocalDateTime getEndDate() { return endDate; }
        public Long getUserId() { return userId; }
    }

    public static class UserOverallReport {
        private final BigDecimal totalExpense;
        private final BigDecimal totalIncome;
        private final BigDecimal balance;
        private final BigDecimal currentMonthSavings;
        private final Long userId;

        public UserOverallReport(BigDecimal totalExpense, BigDecimal totalIncome, BigDecimal balance,
                                BigDecimal currentMonthSavings, Long userId) {
            this.totalExpense = totalExpense;
            this.totalIncome = totalIncome;
            this.balance = balance;
            this.currentMonthSavings = currentMonthSavings;
            this.userId = userId;
        }

        public BigDecimal getTotalExpense() { return totalExpense; }
        public BigDecimal getTotalIncome() { return totalIncome; }
        public BigDecimal getBalance() { return balance; }
        public BigDecimal getCurrentMonthSavings() { return currentMonthSavings; }
        public Long getUserId() { return userId; }
    }
}