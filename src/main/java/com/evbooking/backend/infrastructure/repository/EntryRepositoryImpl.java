package com.evbooking.backend.infrastructure.repository;

import com.evbooking.backend.domain.model.Entry;
import com.evbooking.backend.domain.model.EntryType;
import com.evbooking.backend.domain.repository.EntryRepository;
import com.evbooking.backend.infrastructure.entity.EntryEntity;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Component
public class EntryRepositoryImpl implements EntryRepository {

    private final JpaEntryRepository jpaEntryRepository;

    public EntryRepositoryImpl(JpaEntryRepository jpaEntryRepository) {
        this.jpaEntryRepository = jpaEntryRepository;
    }

    @Override
    public Optional<Entry> findById(Long id) {
        return jpaEntryRepository.findById(id).map(this::toDomain);
    }

    @Override
    public Page<Entry> findByBookId(Long bookId, Pageable pageable) {
        Page<EntryEntity> entityPage = jpaEntryRepository.findByBookIdOrderByDateTimeDesc(bookId, pageable);
        List<Entry> entries = entityPage.getContent().stream()
                .map(this::toDomain)
                .collect(Collectors.toList());
        return new PageImpl<>(entries, pageable, entityPage.getTotalElements());
    }

    @Override
    public List<Entry> findByBookId(Long bookId) {
        return jpaEntryRepository.findByBookIdOrderByDateTimeDesc(bookId)
                .stream()
                .map(this::toDomain)
                .collect(Collectors.toList());
    }

    @Override
    public List<Entry> findByBookIdAndDateTimeBetween(Long bookId, LocalDateTime startDate, LocalDateTime endDate) {
        return jpaEntryRepository.findByBookIdAndDateTimeBetweenOrderByDateTimeDesc(bookId, startDate, endDate)
                .stream()
                .map(this::toDomain)
                .collect(Collectors.toList());
    }

    @Override
    public Entry save(Entry entry) {
        EntryEntity entity = toEntity(entry);
        EntryEntity saved = jpaEntryRepository.save(entity);
        return toDomain(saved);
    }

    @Override
    public void deleteById(Long id) {
        jpaEntryRepository.deleteById(id);
    }

    @Override
    public boolean existsById(Long id) {
        return jpaEntryRepository.existsById(id);
    }

    @Override
    public BigDecimal getTotalExpensesByBookId(Long bookId) {
        return jpaEntryRepository.getTotalAmountByBookIdAndType(bookId, EntryType.EXPENSE);
    }

    @Override
    public BigDecimal getTotalIncomeByBookId(Long bookId) {
        return jpaEntryRepository.getTotalAmountByBookIdAndType(bookId, EntryType.INCOME);
    }

    @Override
    public BigDecimal getTotalExpensesByBookIdAndDateRange(Long bookId, LocalDateTime startDate, LocalDateTime endDate) {
        return jpaEntryRepository.getTotalAmountByBookIdAndTypeAndDateRange(bookId, EntryType.EXPENSE, startDate, endDate);
    }

    @Override
    public BigDecimal getTotalIncomeByBookIdAndDateRange(Long bookId, LocalDateTime startDate, LocalDateTime endDate) {
        return jpaEntryRepository.getTotalAmountByBookIdAndTypeAndDateRange(bookId, EntryType.INCOME, startDate, endDate);
    }

    @Override
    public Optional<LocalDateTime> getLatestEntryDateTimeByBookId(Long bookId) {
        LocalDateTime latest = jpaEntryRepository.findLatestEntryDateTimeByBookId(bookId);
        return Optional.ofNullable(latest);
    }

    @Override
    public BigDecimal getTotalExpensesByUserIdAndDateRange(Long userId, LocalDateTime startDate, LocalDateTime endDate) {
        return jpaEntryRepository.getTotalAmountByUserIdAndTypeAndDateRange(userId, EntryType.EXPENSE, startDate, endDate);
    }

    @Override
    public BigDecimal getTotalIncomeByUserIdAndDateRange(Long userId, LocalDateTime startDate, LocalDateTime endDate) {
        return jpaEntryRepository.getTotalAmountByUserIdAndTypeAndDateRange(userId, EntryType.INCOME, startDate, endDate);
    }

    @Override
    public List<Entry> findByUserIdAndDateTimeBetween(Long userId, LocalDateTime startDate, LocalDateTime endDate) {
        return jpaEntryRepository.findByUserIdAndDateTimeBetweenOrderByDateTimeDesc(userId, startDate, endDate)
                .stream()
                .map(this::toDomain)
                .collect(Collectors.toList());
    }

    @Override
    public BigDecimal getTotalExpensesByUserId(Long userId) {
        return jpaEntryRepository.getTotalAmountByUserIdAndType(userId, EntryType.EXPENSE);
    }

    @Override
    public BigDecimal getTotalIncomeByUserId(Long userId) {
        return jpaEntryRepository.getTotalAmountByUserIdAndType(userId, EntryType.INCOME);
    }

    private Entry toDomain(EntryEntity entity) {
        Entry entry = new Entry();
        entry.setId(entity.getId());
        entry.setBookId(entity.getBookId());
        entry.setType(entity.getType());
        entry.setName(entity.getName());
        entry.setAmount(entity.getAmount());
        entry.setDateTime(entity.getDateTime());
        entry.setCreatedAt(entity.getCreatedAt());
        entry.setUpdatedAt(entity.getUpdatedAt());
        return entry;
    }

    private EntryEntity toEntity(Entry entry) {
        EntryEntity entity = new EntryEntity();
        entity.setId(entry.getId());
        entity.setBookId(entry.getBookId());
        entity.setType(entry.getType());
        entity.setName(entry.getName());
        entity.setAmount(entry.getAmount());
        entity.setDateTime(entry.getDateTime());
        entity.setCreatedAt(entry.getCreatedAt());
        entity.setUpdatedAt(entry.getUpdatedAt());
        return entity;
    }
}