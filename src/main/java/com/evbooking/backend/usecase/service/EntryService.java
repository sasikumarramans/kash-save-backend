package com.evbooking.backend.usecase.service;

import com.evbooking.backend.domain.model.Entry;
import com.evbooking.backend.domain.model.EntryType;
import com.evbooking.backend.domain.repository.EntryRepository;
import com.evbooking.backend.domain.service.EntryDomainService;
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
                           LocalDateTime dateTime, Long userId) {
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

        Entry entry = new Entry(bookId, type, name.trim(), amount, dateTime);
        return entryRepository.save(entry);
    }

    public Entry updateEntry(Long entryId, EntryType type, String name, BigDecimal amount,
                           LocalDateTime dateTime, Long userId) {
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

        if (dateTime != null) {
            entry.setDateTime(dateTime);
        }

        return entryRepository.save(entry);
    }

    public Page<Entry> getEntriesByBookId(Long bookId, Long userId, Pageable pageable) {
        if (bookId == null) {
            throw new RuntimeException("Book ID is required");
        }

        if (userId == null) {
            throw new RuntimeException("User ID is required");
        }

        if (!bookService.verifyBookOwnership(bookId, userId)) {
            throw new RuntimeException("You can only view entries from your own books");
        }

        return entryRepository.findByBookId(bookId, pageable);
    }

    public Optional<Entry> getEntryById(Long entryId, Long userId) {
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

    public void deleteEntry(Long entryId, Long userId) {
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
                                                     LocalDateTime endDate, Long userId) {
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
}