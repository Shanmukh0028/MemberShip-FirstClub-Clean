package com.firstclub.membership.service;

import java.math.BigDecimal;

/**
 * Anti-corruption layer representing the Order domain.
 */
public interface OrderService {

    /**
     * Accumulates an order's value into the user's running monthly total.
     * Called by {@link TierEvaluationService} each time a webhook event arrives.
     */
    void recordOrder(String userId, BigDecimal orderValue);

    /**
     * Returns the user's cumulative order spend for the current month.
     * Used by {@link com.firstclub.membership.strategy.OrderValueStrategy} for
     * tier evaluation.
     */
    BigDecimal getMonthlySpend(String userId);
}
