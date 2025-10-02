package com.evbooking.backend.infrastructure.repository.split;

import com.evbooking.backend.domain.model.split.SplitExpense;
import com.evbooking.backend.domain.repository.split.SplitExpenseRepository;
import com.evbooking.backend.infrastructure.mapper.split.SplitExpenseMapper;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public class SplitExpenseRepositoryImpl implements SplitExpenseRepository {

    private final JpaSplitExpenseRepository jpaSplitExpenseRepository;
    private final SplitExpenseMapper splitExpenseMapper;

    public SplitExpenseRepositoryImpl(JpaSplitExpenseRepository jpaSplitExpenseRepository, SplitExpenseMapper splitExpenseMapper) {
        this.jpaSplitExpenseRepository = jpaSplitExpenseRepository;
        this.splitExpenseMapper = splitExpenseMapper;
    }

    @Override
    public SplitExpense save(SplitExpense splitExpense) {
        var entity = splitExpenseMapper.toEntity(splitExpense);
        var savedEntity = jpaSplitExpenseRepository.save(entity);
        return splitExpenseMapper.toDomain(savedEntity);
    }

    @Override
    public Optional<SplitExpense> findById(Long id) {
        return jpaSplitExpenseRepository.findById(id)
                .map(splitExpenseMapper::toDomain);
    }

    @Override
    public Page<SplitExpense> findByUserId(Long userId, Pageable pageable) {
        return jpaSplitExpenseRepository.findByUserId(userId, pageable)
                .map(splitExpenseMapper::toDomain);
    }

    @Override
    public Page<SplitExpense> findIndividualExpensesByUserId(Long userId, Pageable pageable) {
        return jpaSplitExpenseRepository.findIndividualExpensesByUserId(userId, pageable)
                .map(splitExpenseMapper::toDomain);
    }

    @Override
    public Page<SplitExpense> findByGroupId(Long groupId, Pageable pageable) {
        return jpaSplitExpenseRepository.findByGroupId(groupId, pageable)
                .map(splitExpenseMapper::toDomain);
    }

    @Override
    public Optional<SplitExpense> findByIdAndUserId(Long expenseId, Long userId) {
        return jpaSplitExpenseRepository.findByIdAndUserId(expenseId, userId)
                .map(splitExpenseMapper::toDomain);
    }

    @Override
    public Page<SplitExpense> findByGroupIdAndUserId(Long groupId, Long userId, Pageable pageable) {
        return jpaSplitExpenseRepository.findByGroupIdAndUserId(groupId, userId, pageable)
                .map(splitExpenseMapper::toDomain);
    }

    @Override
    public List<SplitExpense> findByCreatedByUserId(Long userId) {
        return jpaSplitExpenseRepository.findByCreatedByUserId(userId)
                .stream()
                .map(splitExpenseMapper::toDomain)
                .toList();
    }

    @Override
    public List<SplitExpense> findByPaidByUserId(Long userId) {
        return jpaSplitExpenseRepository.findByPaidByUserId(userId)
                .stream()
                .map(splitExpenseMapper::toDomain)
                .toList();
    }

    @Override
    public void deleteById(Long id) {
        jpaSplitExpenseRepository.deleteById(id);
    }
}