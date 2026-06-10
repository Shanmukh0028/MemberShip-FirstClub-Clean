package com.firstclub.membership.strategy;

import com.firstclub.membership.domain.enums.TierLevel;
import com.firstclub.membership.dto.request.OrderCompletedEvent;
import com.firstclub.membership.service.OrderService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.util.Optional;

/**
 * Evaluates tier eligibility based on the user's cumulative monthly spend.
 *
 * <pre>
 *   monthlySpend >= ₹20,000  → PLATINUM
 *   monthlySpend >=  ₹5,000  → GOLD
 *   monthlySpend >=  ₹1,000  → SILVER
 * </pre>
 */
@Component
public class OrderValueStrategy implements TierEvaluationStrategy {

    private static final Logger log = LoggerFactory.getLogger(OrderValueStrategy.class);

    private static final BigDecimal PLATINUM_THRESHOLD = new BigDecimal("20000");
    private static final BigDecimal GOLD_THRESHOLD     = new BigDecimal("5000");
    private static final BigDecimal SILVER_THRESHOLD   = new BigDecimal("1000");

    private final OrderService orderService;

    public OrderValueStrategy(OrderService orderService) {
        this.orderService = orderService;
    }

    @Override
    public Optional<TierLevel> evaluate(OrderCompletedEvent event) {
        BigDecimal monthlySpend = orderService.getMonthlySpend(event.userId());
        log.debug("[{}] userId={} monthlySpend={}", getStrategyName(), event.userId(), monthlySpend);

        if (monthlySpend.compareTo(PLATINUM_THRESHOLD) >= 0) return Optional.of(TierLevel.PLATINUM);
        if (monthlySpend.compareTo(GOLD_THRESHOLD)     >= 0) return Optional.of(TierLevel.GOLD);
        if (monthlySpend.compareTo(SILVER_THRESHOLD)   >= 0) return Optional.of(TierLevel.SILVER);
        return Optional.empty();
    }

    @Override
    public String getStrategyName() {
        return "ORDER_VALUE_STRATEGY";
    }
}
