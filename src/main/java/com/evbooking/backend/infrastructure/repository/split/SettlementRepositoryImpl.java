package com.evbooking.backend.infrastructure.repository.split;

import com.evbooking.backend.domain.model.split.Settlement;
import com.evbooking.backend.domain.repository.split.SettlementRepository;
import com.evbooking.backend.infrastructure.mapper.split.SettlementMapper;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public class SettlementRepositoryImpl implements SettlementRepository {

    private final JpaSettlementRepository jpaSettlementRepository;
    private final SettlementMapper settlementMapper;

    public SettlementRepositoryImpl(JpaSettlementRepository jpaSettlementRepository, SettlementMapper settlementMapper) {
        this.jpaSettlementRepository = jpaSettlementRepository;
        this.settlementMapper = settlementMapper;
    }

    @Override
    public Settlement save(Settlement settlement) {
        var entity = settlementMapper.toEntity(settlement);
        var savedEntity = jpaSettlementRepository.save(entity);
        return settlementMapper.toDomain(savedEntity);
    }

    @Override
    public Optional<Settlement> findById(Long id) {
        return jpaSettlementRepository.findById(id)
                .map(settlementMapper::toDomain);
    }

    @Override
    public Page<Settlement> findByUserId(Long userId, Pageable pageable) {
        return jpaSettlementRepository.findByUserId(userId, pageable)
                .map(settlementMapper::toDomain);
    }

    @Override
    public List<Settlement> findByFromUserId(Long fromUserId) {
        return jpaSettlementRepository.findByFromUserId(fromUserId)
                .stream()
                .map(settlementMapper::toDomain)
                .toList();
    }

    @Override
    public List<Settlement> findByToUserId(Long toUserId) {
        return jpaSettlementRepository.findByToUserId(toUserId)
                .stream()
                .map(settlementMapper::toDomain)
                .toList();
    }

    @Override
    public List<Settlement> findBetweenUsers(Long userId1, Long userId2) {
        return jpaSettlementRepository.findBetweenUsers(userId1, userId2)
                .stream()
                .map(settlementMapper::toDomain)
                .toList();
    }

    @Override
    public List<Settlement> findBySplitExpenseId(Long splitExpenseId) {
        return jpaSettlementRepository.findBySplitExpenseId(splitExpenseId)
                .stream()
                .map(settlementMapper::toDomain)
                .toList();
    }

    @Override
    public List<Settlement> findByGroupId(Long groupId) {
        return jpaSettlementRepository.findByGroupId(groupId)
                .stream()
                .map(settlementMapper::toDomain)
                .toList();
    }
}