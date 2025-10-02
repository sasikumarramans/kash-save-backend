package com.evbooking.backend.infrastructure.mapper.split;

import com.evbooking.backend.domain.model.split.Settlement;
import com.evbooking.backend.infrastructure.entity.split.SettlementEntity;
import org.springframework.stereotype.Component;

@Component
public class SettlementMapper {

    public Settlement toDomain(SettlementEntity entity) {
        if (entity == null) {
            return null;
        }

        Settlement settlement = new Settlement(
                entity.getFromUserId(),
                entity.getToUserId(),
                entity.getAmount(),
                entity.getCurrency(),
                entity.getSplitExpenseId(),
                entity.getNotes(),
                entity.getRecordedByUserId()
        );

        settlement.setId(entity.getId());
        settlement.setSettlementDate(entity.getSettlementDate());

        return settlement;
    }

    public SettlementEntity toEntity(Settlement domain) {
        if (domain == null) {
            return null;
        }

        SettlementEntity entity = new SettlementEntity(
                domain.getFromUserId(),
                domain.getToUserId(),
                domain.getAmount(),
                domain.getCurrency(),
                domain.getSplitExpenseId(),
                domain.getNotes(),
                domain.getRecordedByUserId()
        );

        entity.setId(domain.getId());
        entity.setSettlementDate(domain.getSettlementDate());

        return entity;
    }
}