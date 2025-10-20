package com.evbooking.backend.domain.repository;

import com.evbooking.backend.domain.model.Book;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import java.util.List;
import java.util.Optional;

public interface BookRepository {
    Optional<Book> findById(Long id);
    List<Book> findByUserId(String userId);
    Page<Book> findByUserId(String userId, Pageable pageable);
    Page<Book> searchByUserIdAndName(String userId, String name, Pageable pageable);
    Book save(Book book);
    void deleteById(Long id);
    boolean existsById(Long id);
}