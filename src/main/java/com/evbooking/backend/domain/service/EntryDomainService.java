package com.evbooking.backend.domain.service;

import com.evbooking.backend.domain.model.Entry;
import com.evbooking.backend.domain.model.EntryType;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Service
public class EntryDomainService {

    public void validateEntryCreation(String name, BigDecimal amount, EntryType type, Long bookId, Long userId) {
        if (name == null || name.trim().isEmpty()) {
            throw new RuntimeException("Entry name is required");
        }

        if (amount == null || amount.compareTo(BigDecimal.ZERO) <= 0) {
            throw new RuntimeException("Entry amount must be positive");
        }

        if (type == null) {
            throw new RuntimeException("Entry type is required");
        }

        if (bookId == null) {
            throw new RuntimeException("Book ID is required");
        }

        if (userId == null) {
            throw new RuntimeException("User ID is required");
        }
    }

    public Entry createEntry(String name, String description, BigDecimal amount, EntryType type,
                           LocalDateTime dateTime, Long bookId, Long userId) {
        validateEntryCreation(name, amount, type, bookId, userId);

        if (dateTime == null) {
            dateTime = LocalDateTime.now();
        }

        return new Entry(bookId, type, name.trim(), amount, dateTime);
    }
}