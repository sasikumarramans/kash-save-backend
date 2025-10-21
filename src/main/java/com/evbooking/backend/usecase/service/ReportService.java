package com.evbooking.backend.usecase.service;

import com.evbooking.backend.domain.model.EntryType;
import com.evbooking.backend.domain.repository.EntryRepository;
import com.evbooking.backend.presentation.dto.CategorySummary;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class ReportService {

    private final EntryRepository entryRepository;
    private final BookService bookService;

    public ReportService(EntryRepository entryRepository, BookService bookService) {
        this.entryRepository = entryRepository;
        this.bookService = bookService;
    }

    public OverallReport getOverallReport(Long bookId, String userId) {
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

    public DateRangeReport getDateRangeReport(Long bookId, LocalDateTime startDate, LocalDateTime endDate, String userId) {
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

    public UserDateRangeReport getUserDateRangeReport(String userId, LocalDateTime startDate, LocalDateTime endDate) {
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

    public UserOverallReport getUserOverallReport(String userId) {
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

    // Category-based reports for user (across all books)
    public CategoryReport getUserCategoryReport(String userId, LocalDateTime startDate, LocalDateTime endDate, String period) {
        if (userId == null) {
            throw new RuntimeException("User ID is required");
        }

        if (startDate == null || endDate == null) {
            throw new RuntimeException("Start date and end date are required");
        }

        BigDecimal totalExpense = entryRepository.getTotalExpensesByUserIdAndDateRange(userId, startDate, endDate);
        BigDecimal totalIncome = entryRepository.getTotalIncomeByUserIdAndDateRange(userId, startDate, endDate);
        BigDecimal balance = totalIncome.subtract(totalExpense);

        List<CategorySummary> expenseCategories = buildCategorySummaries(
            entryRepository.getCategoryDataByUserIdAndDateRange(userId, EntryType.EXPENSE.name(), startDate, endDate),
            totalExpense
        );

        List<CategorySummary> incomeCategories = buildCategorySummaries(
            entryRepository.getCategoryDataByUserIdAndDateRange(userId, EntryType.INCOME.name(), startDate, endDate),
            totalIncome
        );

        return new CategoryReport(totalExpense, totalIncome, balance, startDate, endDate, period,
                                 expenseCategories, incomeCategories);
    }

    // Category-based reports for specific book
    public CategoryReport getBookCategoryReport(Long bookId, String userId, LocalDateTime startDate, LocalDateTime endDate, String period) {
        if (bookId == null) {
            throw new RuntimeException("Book ID is required");
        }

        if (userId == null) {
            throw new RuntimeException("User ID is required");
        }

        if (!bookService.verifyBookOwnership(bookId, userId)) {
            throw new RuntimeException("You can only view reports for your own books");
        }

        if (startDate == null || endDate == null) {
            throw new RuntimeException("Start date and end date are required");
        }

        BigDecimal totalExpense = entryRepository.getTotalExpensesByBookIdAndDateRange(bookId, startDate, endDate);
        BigDecimal totalIncome = entryRepository.getTotalIncomeByBookIdAndDateRange(bookId, startDate, endDate);
        BigDecimal balance = totalIncome.subtract(totalExpense);

        List<CategorySummary> expenseCategories = buildCategorySummaries(
            entryRepository.getCategoryDataByBookIdAndDateRange(bookId, EntryType.EXPENSE.name(), startDate, endDate),
            totalExpense
        );

        List<CategorySummary> incomeCategories = buildCategorySummaries(
            entryRepository.getCategoryDataByBookIdAndDateRange(bookId, EntryType.INCOME.name(), startDate, endDate),
            totalIncome
        );

        return new CategoryReport(totalExpense, totalIncome, balance, startDate, endDate, period,
                                 expenseCategories, incomeCategories);
    }

    private List<CategorySummary> buildCategorySummaries(List<EntryRepository.CategoryData> categoryDataList, BigDecimal total) {
        if (total.compareTo(BigDecimal.ZERO) == 0) {
            return categoryDataList.stream()
                .map(data -> new CategorySummary(
                    data.getName(),
                    data.getTotalAmount(),
                    0.0,
                    data.getCount()
                ))
                .collect(Collectors.toList());
        }

        return categoryDataList.stream()
            .map(data -> {
                double percentage = data.getTotalAmount()
                    .divide(total, 4, RoundingMode.HALF_UP)
                    .multiply(BigDecimal.valueOf(100))
                    .setScale(2, RoundingMode.HALF_UP)
                    .doubleValue();

                return new CategorySummary(
                    data.getName(),
                    data.getTotalAmount(),
                    percentage,
                    data.getCount()
                );
            })
            .collect(Collectors.toList());
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
        private final String userId;

        public UserDateRangeReport(BigDecimal totalExpense, BigDecimal totalIncome, BigDecimal balance,
                                  LocalDateTime startDate, LocalDateTime endDate, String userId) {
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
        public String getUserId() { return userId; }
    }

    public static class UserOverallReport {
        private final BigDecimal totalExpense;
        private final BigDecimal totalIncome;
        private final BigDecimal balance;
        private final BigDecimal currentMonthSavings;
        private final String userId;

        public UserOverallReport(BigDecimal totalExpense, BigDecimal totalIncome, BigDecimal balance,
                                BigDecimal currentMonthSavings, String userId) {
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
        public String getUserId() { return userId; }
    }

    public static class CategoryReport {
        private final BigDecimal totalExpense;
        private final BigDecimal totalIncome;
        private final BigDecimal balance;
        private final LocalDateTime startDate;
        private final LocalDateTime endDate;
        private final String period;
        private final List<CategorySummary> expenseCategories;
        private final List<CategorySummary> incomeCategories;

        public CategoryReport(BigDecimal totalExpense, BigDecimal totalIncome, BigDecimal balance,
                            LocalDateTime startDate, LocalDateTime endDate, String period,
                            List<CategorySummary> expenseCategories, List<CategorySummary> incomeCategories) {
            this.totalExpense = totalExpense;
            this.totalIncome = totalIncome;
            this.balance = balance;
            this.startDate = startDate;
            this.endDate = endDate;
            this.period = period;
            this.expenseCategories = expenseCategories;
            this.incomeCategories = incomeCategories;
        }

        public BigDecimal getTotalExpense() { return totalExpense; }
        public BigDecimal getTotalIncome() { return totalIncome; }
        public BigDecimal getBalance() { return balance; }
        public LocalDateTime getStartDate() { return startDate; }
        public LocalDateTime getEndDate() { return endDate; }
        public String getPeriod() { return period; }
        public List<CategorySummary> getExpenseCategories() { return expenseCategories; }
        public List<CategorySummary> getIncomeCategories() { return incomeCategories; }
    }
}