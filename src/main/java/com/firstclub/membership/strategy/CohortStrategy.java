package com.firstclub.membership.strategy;

import com.firstclub.membership.domain.enums.TierLevel;
import com.firstclub.membership.dto.request.OrderCompletedEvent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.util.Optional;
import java.util.Set;

/**
 * Upgrades tier based on the user's cohort membership.
 *
 * <p>For this demo, cohort membership is determined by userId prefix:
 * <ul>
 *   <li>{@code vip_*} or {@code platinum_*} → PLATINUM</li>
 *   <li>{@code gold_*} or {@code premium_*} → GOLD</li>
 * </ul>
 */
@Component
public class CohortStrategy implements TierEvaluationStrategy {

    private static final Logger log = LoggerFactory.getLogger(CohortStrategy.class);

    private static final Set<String> PLATINUM_PREFIXES = Set.of("vip_", "platinum_");
    private static final Set<String> GOLD_PREFIXES     = Set.of("gold_", "premium_");

    @Override
    public Optional<TierLevel> evaluate(OrderCompletedEvent event) {
        String userId = event.userId().toLowerCase();
        log.debug("[{}] evaluating userId={}", getStrategyName(), userId);

        if (PLATINUM_PREFIXES.stream().anyMatch(userId::startsWith)) {
            return Optional.of(TierLevel.PLATINUM);
        }
        if (GOLD_PREFIXES.stream().anyMatch(userId::startsWith)) {
            return Optional.of(TierLevel.GOLD);
        }
        return Optional.empty();
    }

    @Override
    public String getStrategyName() {
        return "COHORT_STRATEGY";
    }
}
