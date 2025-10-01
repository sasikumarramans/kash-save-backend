package com.evbooking.backend.domain.model.split;

public enum SplitActivityType {
    // Group activities
    GROUP_CREATED,
    GROUP_DELETED,
    GROUP_UPDATED,
    MEMBER_ADDED,
    MEMBER_REMOVED,
    MEMBER_LEFT,
    ADMIN_CHANGED,

    // Expense activities
    EXPENSE_CREATED,
    EXPENSE_UPDATED,
    EXPENSE_DELETED,
    SPLIT_RECALCULATED,

    // Settlement activities
    SETTLEMENT_RECORDED,
    PARTICIPANT_SETTLED,
    PARTICIPANT_UNSETTLED,
    DEBT_FORGIVEN,

    // Friend activities
    FRIEND_ADDED,
    FRIEND_REMOVED,

    // Payment activities
    PAYMENT_REQUEST_SENT,
    PAYMENT_REQUEST_RECEIVED
}