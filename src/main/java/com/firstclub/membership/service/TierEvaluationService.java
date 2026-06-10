package com.firstclub.membership.service;

import com.firstclub.membership.domain.entity.Tier;
import com.firstclub.membership.domain.enums.TierLevel;
import com.firstclub.membership.dto.request.OrderCompletedEvent;
import com.firstclub.membership.exception.TierNotFoundException;
import com.firstclub.membership.repository.TierRepository;
import com.firstclub.membership.strategy.TierEvaluationStrategy;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.dao.OptimisticLockingFailureException;
import org.springframework.stereotype.Service;

import java.util.Comparator;
import java.util.List;
import java.util.Optional;

/**
 * Orchestrates all registered {@link TierEvaluationStrategy} implementations.
 */
@Service
public class TierEvaluationService {

    private static final Logger log = LoggerFactory.getLogger(TierEvaluationService.class);
    private static final int MAX_RETRY_ATTEMPTS = 3;

    private final List<TierEvaluationStrategy> strategies;
    private final TierRepository tierRepository;
    private final SubscriptionService subscriptionService;
    private final OrderService orderService;

    public TierEvaluationService(List<TierEvaluationStrategy> strategies,
                                 TierRepository tierRepository,
                                 SubscriptionService subscriptionService,
                                 OrderService orderService) {
        this.strategies = strategies;
        this.tierRepository = tierRepository;
        this.subscriptionService = subscriptionService;
        this.orderService = orderService;
    }

    /**
     * Evaluates the event using all strategies and upgrades the user's tier if warranted.
     */
    public void evaluate(OrderCompletedEvent event) {
        log.info("Tier evaluation triggered for userId={}, orderId={}", event.userId(), event.orderId());

        // Accumulate this order into the monthly spend total BEFORE strategies read it.
        orderService.recordOrder(event.userId(), event.orderValue());
        log.debug("Monthly spend after this order for userId={}: {}",
                event.userId(), orderService.getMonthlySpend(event.userId()));

        Optional<TierLevel> bestTier = strategies.stream()
                .peek(s -> log.debug("Running strategy: {}", s.getStrategyName()))
                .map(s -> s.evaluate(event))
                .filter(Optional::isPresent)
                .map(Optional::get)
                .max(Comparator.comparingInt(TierLevel::getRank));

        if (bestTier.isEmpty()) {
            log.info("No tier upgrade warranted for userId={}", event.userId());
            return;
        }

        TierLevel targetLevel = bestTier.get();
        log.info("Best qualifying tier for userId={} is {}", event.userId(), targetLevel);

        Tier newTier = tierRepository.findByLevel(targetLevel)
                .orElseThrow(() -> new TierNotFoundException(targetLevel));

        attemptUpgrade(event.userId(), newTier, 1);
    }

    private void attemptUpgrade(String userId, Tier newTier, int attempt) {
        try {
            subscriptionService.upgradeTier(userId, newTier);
        } catch (OptimisticLockingFailureException ex) {
            if (attempt < MAX_RETRY_ATTEMPTS) {
                log.warn("Optimistic lock conflict upgrading tier for userId={}, attempt {}/{}. Retrying...",
                        userId, attempt, MAX_RETRY_ATTEMPTS);
                attemptUpgrade(userId, newTier, attempt + 1);
            } else {
                log.error("Tier upgrade for userId={} failed after {} attempts due to concurrent modification.",
                        userId, MAX_RETRY_ATTEMPTS);
                throw ex;
            }
        }
    }
}
