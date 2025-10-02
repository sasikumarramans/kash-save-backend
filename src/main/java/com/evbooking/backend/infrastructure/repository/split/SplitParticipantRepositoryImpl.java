package com.evbooking.backend.infrastructure.repository.split;

import com.evbooking.backend.domain.model.split.SplitParticipant;
import com.evbooking.backend.domain.repository.split.SplitParticipantRepository;
import com.evbooking.backend.infrastructure.mapper.split.SplitParticipantMapper;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public class SplitParticipantRepositoryImpl implements SplitParticipantRepository {

    private final JpaSplitParticipantRepository jpaSplitParticipantRepository;
    private final SplitParticipantMapper splitParticipantMapper;

    public SplitParticipantRepositoryImpl(JpaSplitParticipantRepository jpaSplitParticipantRepository, SplitParticipantMapper splitParticipantMapper) {
        this.jpaSplitParticipantRepository = jpaSplitParticipantRepository;
        this.splitParticipantMapper = splitParticipantMapper;
    }

    @Override
    public SplitParticipant save(SplitParticipant splitParticipant) {
        var entity = splitParticipantMapper.toEntity(splitParticipant);
        var savedEntity = jpaSplitParticipantRepository.save(entity);
        return splitParticipantMapper.toDomain(savedEntity);
    }

    @Override
    public List<SplitParticipant> findBySplitExpenseId(Long splitExpenseId) {
        return jpaSplitParticipantRepository.findBySplitExpenseId(splitExpenseId)
                .stream()
                .map(splitParticipantMapper::toDomain)
                .toList();
    }

    @Override
    public List<SplitParticipant> findByUserId(Long userId) {
        return jpaSplitParticipantRepository.findByUserId(userId)
                .stream()
                .map(splitParticipantMapper::toDomain)
                .toList();
    }

    @Override
    public Optional<SplitParticipant> findBySplitExpenseIdAndUserId(Long expenseId, Long userId) {
        return jpaSplitParticipantRepository.findBySplitExpenseIdAndUserId(expenseId, userId)
                .map(splitParticipantMapper::toDomain);
    }

    @Override
    public List<SplitParticipant> findByUserIdAndIsSettledFalse(Long userId) {
        return jpaSplitParticipantRepository.findByUserIdAndIsSettledFalse(userId)
                .stream()
                .map(splitParticipantMapper::toDomain)
                .toList();
    }

    @Override
    public List<SplitParticipant> findByGroupId(Long groupId) {
        return jpaSplitParticipantRepository.findByGroupId(groupId)
                .stream()
                .map(splitParticipantMapper::toDomain)
                .toList();
    }

    @Override
    public void deleteBySplitExpenseId(Long splitExpenseId) {
        jpaSplitParticipantRepository.deleteBySplitExpenseId(splitExpenseId);
    }
}