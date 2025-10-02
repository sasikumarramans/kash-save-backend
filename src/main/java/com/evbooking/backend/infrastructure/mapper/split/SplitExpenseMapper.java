package com.evbooking.backend.infrastructure.mapper.split;

import com.evbooking.backend.domain.model.split.SplitExpense;
import com.evbooking.backend.infrastructure.entity.split.SplitExpenseEntity;
import org.springframework.stereotype.Component;

@Component
public class SplitExpenseMapper {

    public SplitExpense toDomain(SplitExpenseEntity entity) {
        if (entity == null) {
            return null;
        }

        SplitExpense expense = new SplitExpense(
                entity.getDescription(),
                entity.getTotalAmount(),
                entity.getCurrency(),
                entity.getPaidByUserId(),
                entity.getGroupId(),
                entity.getSplitType(),
                entity.getCreatedByUserId()
        );

        expense.setId(entity.getId());
        expense.setCreatedAt(entity.getCreatedAt());
        expense.setUpdatedAt(entity.getUpdatedAt());

        return expense;
    }

    public SplitExpenseEntity toEntity(SplitExpense domain) {
        if (domain == null) {
            return null;
        }

        SplitExpenseEntity entity = new SplitExpenseEntity(
                domain.getDescription(),
                domain.getTotalAmount(),
                domain.getCurrency(),
                domain.getPaidByUserId(),
                domain.getGroupId(),
                domain.getSplitType(),
                domain.getCreatedByUserId()
        );

        entity.setId(domain.getId());
        entity.setCreatedAt(domain.getCreatedAt());
        entity.setUpdatedAt(domain.getUpdatedAt());

        return entity;
    }
}