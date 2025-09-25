package com.evbooking.backend.domain.service;

import com.evbooking.backend.domain.model.Book;
import org.springframework.stereotype.Service;

@Service
public class BookDomainService {

    public void validateBookCreation(String name, Long userId) {
        if (name == null || name.trim().isEmpty()) {
            throw new RuntimeException("Book name is required");
        }

        if (userId == null) {
            throw new RuntimeException("User ID is required");
        }
    }

    public void validateBookOwnership(Book book, Long userId) {
        if (!book.getUserId().equals(userId)) {
            throw new RuntimeException("You can only access your own books");
        }
    }

    public Book createBook(String name, String description, Long userId) {
        validateBookCreation(name, userId);
        return new Book(name.trim(), description, userId);
    }

    public Book createBook(String name, Long userId) {
        return createBook(name, null, userId);
    }
}