package com.evbooking.backend.infrastructure.repository;

import com.evbooking.backend.domain.model.Book;
import com.evbooking.backend.domain.repository.BookRepository;
import com.evbooking.backend.infrastructure.entity.BookEntity;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;

@Component
public class BookRepositoryImpl implements BookRepository {

    private final JpaBookRepository jpaBookRepository;

    public BookRepositoryImpl(JpaBookRepository jpaBookRepository) {
        this.jpaBookRepository = jpaBookRepository;
    }

    @Override
    public Optional<Book> findById(Long id) {
        return jpaBookRepository.findById(id).map(this::toDomain);
    }

    @Override
    public List<Book> findByUserId(Long userId) {
        return jpaBookRepository.findByUserIdOrderByCreatedAtDesc(userId)
                .stream()
                .map(this::toDomain)
                .collect(Collectors.toList());
    }

    @Override
    public Page<Book> findByUserId(Long userId, Pageable pageable) {
        Page<BookEntity> entityPage = jpaBookRepository.findByUserIdOrderByCreatedAtDesc(userId, pageable);
        List<Book> books = entityPage.getContent().stream()
                .map(this::toDomain)
                .collect(Collectors.toList());
        return new PageImpl<>(books, pageable, entityPage.getTotalElements());
    }

    @Override
    public Book save(Book book) {
        BookEntity entity = toEntity(book);
        BookEntity saved = jpaBookRepository.save(entity);
        return toDomain(saved);
    }

    @Override
    public void deleteById(Long id) {
        jpaBookRepository.deleteById(id);
    }

    @Override
    public boolean existsById(Long id) {
        return jpaBookRepository.existsById(id);
    }

    private Book toDomain(BookEntity entity) {
        Book book = new Book();
        book.setId(entity.getId());
        book.setName(entity.getName());
        book.setDescription(entity.getDescription());
        book.setUserId(entity.getUserId());
        book.setCreatedAt(entity.getCreatedAt());
        book.setUpdatedAt(entity.getUpdatedAt());
        return book;
    }

    private BookEntity toEntity(Book book) {
        BookEntity entity = new BookEntity();
        entity.setId(book.getId());
        entity.setName(book.getName());
        entity.setDescription(book.getDescription());
        entity.setUserId(book.getUserId());
        entity.setCreatedAt(book.getCreatedAt());
        entity.setUpdatedAt(book.getUpdatedAt());
        return entity;
    }
}