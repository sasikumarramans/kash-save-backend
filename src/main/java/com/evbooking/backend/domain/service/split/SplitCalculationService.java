package com.evbooking.backend.domain.service.split;

import com.evbooking.backend.domain.model.split.SplitType;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
public class SplitCalculationService {

    public Map<Long, BigDecimal> calculateSplitAmounts(BigDecimal totalAmount, SplitType splitType,
                                                      Map<Long, BigDecimal> participantSplitValues) {
        return switch (splitType) {
            case EQUAL -> calculateEqualSplit(totalAmount, participantSplitValues.keySet().stream().toList());
            case PERCENTAGE -> calculatePercentageSplit(totalAmount, participantSplitValues);
            case EXACT -> validateExactSplit(totalAmount, participantSplitValues);
            case SHARES -> calculateSharesSplit(totalAmount, participantSplitValues);
        };
    }

    private Map<Long, BigDecimal> calculateEqualSplit(BigDecimal totalAmount, List<Long> participantIds) {
        Map<Long, BigDecimal> result = new HashMap<>();
        int participantCount = participantIds.size();

        if (participantCount == 0) {
            throw new RuntimeException("At least one participant is required");
        }

        BigDecimal equalAmount = totalAmount.divide(BigDecimal.valueOf(participantCount), 2, RoundingMode.HALF_UP);

        // Handle rounding differences
        BigDecimal totalCalculated = equalAmount.multiply(BigDecimal.valueOf(participantCount));
        BigDecimal difference = totalAmount.subtract(totalCalculated);

        for (int i = 0; i < participantIds.size(); i++) {
            Long participantId = participantIds.get(i);
            BigDecimal amount = equalAmount;

            // Add rounding difference to the first participant
            if (i == 0 && difference.compareTo(BigDecimal.ZERO) != 0) {
                amount = amount.add(difference);
            }

            result.put(participantId, amount);
        }

        return result;
    }

    private Map<Long, BigDecimal> calculatePercentageSplit(BigDecimal totalAmount, Map<Long, BigDecimal> percentages) {
        // Validate percentages sum to 100
        BigDecimal totalPercentage = percentages.values().stream()
            .reduce(BigDecimal.ZERO, BigDecimal::add);

        if (totalPercentage.compareTo(BigDecimal.valueOf(100)) != 0) {
            throw new RuntimeException("Percentages must sum to 100%. Current total: " + totalPercentage + "%");
        }

        Map<Long, BigDecimal> result = new HashMap<>();
        BigDecimal totalCalculated = BigDecimal.ZERO;

        // Calculate amounts for all but the last participant
        List<Long> participantIds = percentages.keySet().stream().toList();
        for (int i = 0; i < participantIds.size() - 1; i++) {
            Long participantId = participantIds.get(i);
            BigDecimal percentage = percentages.get(participantId);
            BigDecimal amount = totalAmount.multiply(percentage)
                .divide(BigDecimal.valueOf(100), 2, RoundingMode.HALF_UP);

            result.put(participantId, amount);
            totalCalculated = totalCalculated.add(amount);
        }

        // Last participant gets the remaining amount to handle rounding
        Long lastParticipant = participantIds.get(participantIds.size() - 1);
        BigDecimal lastAmount = totalAmount.subtract(totalCalculated);
        result.put(lastParticipant, lastAmount);

        return result;
    }

    private Map<Long, BigDecimal> validateExactSplit(BigDecimal totalAmount, Map<Long, BigDecimal> exactAmounts) {
        BigDecimal totalSpecified = exactAmounts.values().stream()
            .reduce(BigDecimal.ZERO, BigDecimal::add);

        if (totalSpecified.compareTo(totalAmount) != 0) {
            throw new RuntimeException("Exact amounts must sum to total amount. " +
                "Expected: " + totalAmount + ", Got: " + totalSpecified);
        }

        return new HashMap<>(exactAmounts);
    }

    private Map<Long, BigDecimal> calculateSharesSplit(BigDecimal totalAmount, Map<Long, BigDecimal> shares) {
        // Validate shares are positive integers
        for (Map.Entry<Long, BigDecimal> entry : shares.entrySet()) {
            BigDecimal share = entry.getValue();
            if (share == null || share.compareTo(BigDecimal.ZERO) <= 0 ||
                share.remainder(BigDecimal.ONE).compareTo(BigDecimal.ZERO) != 0) {
                throw new RuntimeException("Shares must be positive whole numbers");
            }
        }

        BigDecimal totalShares = shares.values().stream()
            .reduce(BigDecimal.ZERO, BigDecimal::add);

        if (totalShares.compareTo(BigDecimal.ZERO) <= 0) {
            throw new RuntimeException("Total shares must be greater than 0");
        }

        BigDecimal amountPerShare = totalAmount.divide(totalShares, 2, RoundingMode.HALF_UP);

        Map<Long, BigDecimal> result = new HashMap<>();
        BigDecimal totalCalculated = BigDecimal.ZERO;

        // Calculate amounts for all but the last participant
        List<Long> participantIds = shares.keySet().stream().toList();
        for (int i = 0; i < participantIds.size() - 1; i++) {
            Long participantId = participantIds.get(i);
            BigDecimal userShares = shares.get(participantId);
            BigDecimal amount = amountPerShare.multiply(userShares);

            result.put(participantId, amount);
            totalCalculated = totalCalculated.add(amount);
        }

        // Last participant gets the remaining amount to handle rounding
        Long lastParticipant = participantIds.get(participantIds.size() - 1);
        BigDecimal lastAmount = totalAmount.subtract(totalCalculated);
        result.put(lastParticipant, lastAmount);

        return result;
    }

    public void validateSplitRequest(BigDecimal totalAmount, SplitType splitType,
                                   Map<Long, BigDecimal> participantSplitValues) {
        if (totalAmount == null || totalAmount.compareTo(BigDecimal.ZERO) <= 0) {
            throw new RuntimeException("Total amount must be greater than 0");
        }

        if (splitType == null) {
            throw new RuntimeException("Split type is required");
        }

        if (participantSplitValues == null || participantSplitValues.isEmpty()) {
            throw new RuntimeException("At least one participant is required");
        }

        // Type-specific validation
        switch (splitType) {
            case PERCENTAGE -> {
                for (BigDecimal percentage : participantSplitValues.values()) {
                    if (percentage == null || percentage.compareTo(BigDecimal.ZERO) < 0 ||
                        percentage.compareTo(BigDecimal.valueOf(100)) > 0) {
                        throw new RuntimeException("Percentages must be between 0 and 100");
                    }
                }
            }
            case EXACT -> {
                for (BigDecimal amount : participantSplitValues.values()) {
                    if (amount == null || amount.compareTo(BigDecimal.ZERO) < 0) {
                        throw new RuntimeException("Exact amounts must be non-negative");
                    }
                }
            }
            case SHARES -> {
                for (BigDecimal shares : participantSplitValues.values()) {
                    if (shares == null || shares.compareTo(BigDecimal.ZERO) <= 0 ||
                        shares.remainder(BigDecimal.ONE).compareTo(BigDecimal.ZERO) != 0) {
                        throw new RuntimeException("Shares must be positive whole numbers");
                    }
                }
            }
        }
    }
}