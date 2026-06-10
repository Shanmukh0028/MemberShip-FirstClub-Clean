package com.firstclub.membership.strategy;

import com.firstclub.membership.domain.enums.TierLevel;
import com.firstclub.membership.dto.request.OrderCompletedEvent;

import java.util.Optional;

/**
 * Strategy interface for evaluating which membership tier a user qualifies for
 * based on an order event.
 */
public interface TierEvaluationStrategy {

    /**
     * Evaluates the event and returns the highest tier the user qualifies for
     * under this strategy's rule, or {@code Optional.empty()} if no tier is earned.
     */
    Optional<TierLevel> evaluate(OrderCompletedEvent event);

    /**
     * Human-readable name used in logs and debugging output.
     */
    String getStrategyName();
}
