package com.evbooking.backend.usecase.service;

import com.evbooking.backend.domain.model.Book;
import com.evbooking.backend.domain.repository.BookRepository;
import com.evbooking.backend.domain.repository.EntryRepository;
import com.evbooking.backend.domain.service.BookDomainService;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Service
public class BookService {

    private final BookRepository bookRepository;
    private final EntryRepository entryRepository;
    private final BookDomainService bookDomainService;

    public BookService(BookRepository bookRepository, EntryRepository entryRepository, BookDomainService bookDomainService) {
        this.bookRepository = bookRepository;
        this.entryRepository = entryRepository;
        this.bookDomainService = bookDomainService;
    }

    public Book createBook(String name, String description, String currency, Long userId) {
        Book book = bookDomainService.createBook(name, description, currency, userId);
        return bookRepository.save(book);
    }

    public List<Book> getBooksByUserId(Long userId) {
        if (userId == null) {
            throw new RuntimeException("User ID is required");
        }

        return bookRepository.findByUserId(userId);
    }

    public Optional<Book> getBookById(Long bookId) {
        if (bookId == null) {
            throw new RuntimeException("Book ID is required");
        }

        return bookRepository.findById(bookId);
    }

    public void deleteBook(Long bookId, Long userId) {
        if (bookId == null) {
            throw new RuntimeException("Book ID is required");
        }

        if (userId == null) {
            throw new RuntimeException("User ID is required");
        }

        Optional<Book> bookOpt = bookRepository.findById(bookId);
        if (bookOpt.isEmpty()) {
            throw new RuntimeException("Book not found");
        }

        Book book = bookOpt.get();
        if (!book.getUserId().equals(userId)) {
            throw new RuntimeException("You can only delete your own books");
        }

        bookRepository.deleteById(bookId);
    }

    public Page<Book> getBooksByUserId(Long userId, Pageable pageable) {
        if (userId == null) {
            throw new RuntimeException("User ID is required");
        }

        return bookRepository.findByUserId(userId, pageable);
    }

    public boolean verifyBookOwnership(Long bookId, Long userId) {
        Optional<Book> book = bookRepository.findById(bookId);
        return book.isPresent() && book.get().getUserId().equals(userId);
    }

    public BookSummary getBookSummary(Long bookId, Long userId) {
        if (!verifyBookOwnership(bookId, userId)) {
            throw new RuntimeException("You can only view summaries for your own books");
        }

        BigDecimal totalExpense = entryRepository.getTotalExpensesByBookId(bookId);
        BigDecimal totalIncome = entryRepository.getTotalIncomeByBookId(bookId);
        Optional<LocalDateTime> lastEntryDateTime = entryRepository.getLatestEntryDateTimeByBookId(bookId);

        return new BookSummary(totalExpense, totalIncome, lastEntryDateTime.orElse(null));
    }

    public static class BookSummary {
        private final BigDecimal totalExpense;
        private final BigDecimal totalIncome;
        private final LocalDateTime lastEntryDateTime;

        public BookSummary(BigDecimal totalExpense, BigDecimal totalIncome, LocalDateTime lastEntryDateTime) {
            this.totalExpense = totalExpense;
            this.totalIncome = totalIncome;
            this.lastEntryDateTime = lastEntryDateTime;
        }

        public BigDecimal getTotalExpense() { return totalExpense; }
        public BigDecimal getTotalIncome() { return totalIncome; }
        public LocalDateTime getLastEntryDateTime() { return lastEntryDateTime; }
    }
}