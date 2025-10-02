package com.evbooking.backend.infrastructure.mapper.split;

import com.evbooking.backend.domain.model.split.SplitParticipant;
import com.evbooking.backend.infrastructure.entity.split.SplitParticipantEntity;
import org.springframework.stereotype.Component;

@Component
public class SplitParticipantMapper {

    public SplitParticipant toDomain(SplitParticipantEntity entity) {
        if (entity == null) {
            return null;
        }

        SplitParticipant participant = new SplitParticipant(
                entity.getSplitExpenseId(),
                entity.getUserId(),
                entity.getAmountOwed(),
                entity.getSplitValue()
        );

        participant.setId(entity.getId());
        participant.setSettled(entity.isSettled());
        participant.setSettledAt(entity.getSettledAt());
        participant.setCreatedAt(entity.getCreatedAt());

        return participant;
    }

    public SplitParticipantEntity toEntity(SplitParticipant domain) {
        if (domain == null) {
            return null;
        }

        SplitParticipantEntity entity = new SplitParticipantEntity(
                domain.getSplitExpenseId(),
                domain.getUserId(),
                domain.getAmountOwed(),
                domain.getSplitValue()
        );

        entity.setId(domain.getId());
        entity.setSettled(domain.isSettled());
        entity.setSettledAt(domain.getSettledAt());
        entity.setCreatedAt(domain.getCreatedAt());

        return entity;
    }
}