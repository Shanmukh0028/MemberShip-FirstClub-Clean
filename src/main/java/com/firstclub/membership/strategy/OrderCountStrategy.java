package com.firstclub.membership.strategy;

import com.firstclub.membership.domain.enums.TierLevel;
import com.firstclub.membership.dto.request.OrderCompletedEvent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.util.Optional;

/**
 * Upgrades tier based on the user's cumulative order count.
 *
 * <pre>
 *   orderCount >= 50  → PLATINUM
 *   orderCount >= 10  → GOLD
 *   orderCount >=  3  → SILVER
 * </pre>
 */
@Component
public class OrderCountStrategy implements TierEvaluationStrategy {

    private static final Logger log = LoggerFactory.getLogger(OrderCountStrategy.class);

    private static final int PLATINUM_THRESHOLD = 50;
    private static final int GOLD_THRESHOLD     = 10;
    private static final int SILVER_THRESHOLD   = 3;

    @Override
    public Optional<TierLevel> evaluate(OrderCompletedEvent event) {
        int count = event.totalOrderCount();
        log.debug("[{}] userId={} totalOrderCount={}", getStrategyName(), event.userId(), count);

        if (count >= PLATINUM_THRESHOLD) return Optional.of(TierLevel.PLATINUM);
        if (count >= GOLD_THRESHOLD)     return Optional.of(TierLevel.GOLD);
        if (count >= SILVER_THRESHOLD)   return Optional.of(TierLevel.SILVER);
        return Optional.empty();
    }

    @Override
    public String getStrategyName() {
        return "ORDER_COUNT_STRATEGY";
    }
}
