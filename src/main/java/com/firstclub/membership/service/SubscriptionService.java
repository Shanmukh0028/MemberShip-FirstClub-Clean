package com.firstclub.membership.service;

import com.firstclub.membership.domain.entity.Plan;
import com.firstclub.membership.domain.entity.Subscription;
import com.firstclub.membership.domain.entity.Tier;
import com.firstclub.membership.domain.enums.SubscriptionStatus;
import com.firstclub.membership.dto.request.CreateSubscriptionRequest;
import com.firstclub.membership.dto.request.ModifySubscriptionRequest;
import com.firstclub.membership.dto.response.SubscriptionResponse;
import com.firstclub.membership.exception.AlreadySubscribedException;
import com.firstclub.membership.exception.PlanNotFoundException;
import com.firstclub.membership.exception.SubscriptionNotFoundException;
import com.firstclub.membership.exception.TierNotFoundException;
import com.firstclub.membership.repository.PlanRepository;
import com.firstclub.membership.repository.SubscriptionRepository;
import com.firstclub.membership.repository.TierRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

@Service
public class SubscriptionService {

    private static final Logger log = LoggerFactory.getLogger(SubscriptionService.class);

    private final SubscriptionRepository subscriptionRepository;
    private final PlanRepository planRepository;
    private final TierRepository tierRepository;

    public SubscriptionService(SubscriptionRepository subscriptionRepository,
                               PlanRepository planRepository,
                               TierRepository tierRepository) {
        this.subscriptionRepository = subscriptionRepository;
        this.planRepository = planRepository;
        this.tierRepository = tierRepository;
    }

    @Transactional(readOnly = true)
    public SubscriptionResponse getSubscription(String userId) {
        log.info("Fetching active subscription for user: {}", userId);
        return SubscriptionResponse.from(findActiveSubscription(userId));
    }

    /**
     * Creates a new subscription for the user.
     *
     * <p><b>Concurrency safety:</b> the {@code active_token} column
     * has a DB-level UNIQUE constraint. The optimistic
     * {@code existsByUserIdAndStatus} check is a fast-path only. The real guard is
     * catching {@link DataIntegrityViolationException} if two threads race past the
     * check simultaneously and both attempt the INSERT.
     */
    @Transactional
    public SubscriptionResponse subscribe(String userId, CreateSubscriptionRequest request) {
        log.info("Subscribing user {} to planId={}, tierId={}", userId, request.planId(), request.tierId());

        // Fast-path check (not race-safe on its own — the DB constraint is the real guard)
        if (subscriptionRepository.existsByUserIdAndStatus(userId, SubscriptionStatus.ACTIVE)) {
            throw new AlreadySubscribedException(userId);
        }

        Plan plan = planRepository.findById(request.planId())
                .orElseThrow(() -> new PlanNotFoundException(request.planId()));
        Tier tier = tierRepository.findById(request.tierId())
                .orElseThrow(() -> new TierNotFoundException(request.tierId()));

        LocalDateTime now = LocalDateTime.now();
        Subscription subscription = Subscription.builder()
                .userId(userId)
                .plan(plan)
                .tier(tier)
                .startDate(now)
                .endDate(now.plusDays(plan.getDurationInDays()))
                .status(SubscriptionStatus.ACTIVE)
                .activeToken(userId)     // claimed slot in the unique index
                .version(0L)
                .build();

        try {
            Subscription saved = subscriptionRepository.save(subscription);
            log.info("Subscription created id={} for user={}", saved.getId(), userId);
            return SubscriptionResponse.from(saved);
        } catch (DataIntegrityViolationException ex) {
            // Race condition: two threads passed the fast-path check; the loser lands here.
            log.warn("Concurrent subscribe detected for userId={} — unique constraint fired", userId);
            throw new AlreadySubscribedException(userId);
        }
    }

    /**
     * Cancels the subscription immediately: status → {@code CANCELLED},
     * {@code activeToken} cleared so the user can re-subscribe right away.
     */
    @Transactional
    public SubscriptionResponse cancel(String userId) {
        log.info("Cancelling subscription for user: {}", userId);
        Subscription sub = findActiveSubscription(userId);
        sub.setStatus(SubscriptionStatus.CANCELLED);
        sub.setActiveToken(null);   // release the unique-index slot
        Subscription saved = subscriptionRepository.save(sub);
        log.info("Subscription id={} cancelled for user={}", saved.getId(), userId);
        return SubscriptionResponse.from(saved);
    }

    /**
     * Modifies an active subscription's plan and/or tier (upgrade/downgrade).
     * At least one of planId or tierId must be provided.
     */
    @Transactional
    public SubscriptionResponse modify(String userId, ModifySubscriptionRequest request) {
        if (request.planId() == null && request.tierId() == null) {
            throw new IllegalArgumentException("At least one of planId or tierId must be provided");
        }

        log.info("Modifying subscription for user={}, newPlanId={}, newTierId={}",
                userId, request.planId(), request.tierId());

        Subscription sub = findActiveSubscription(userId);

        if (request.planId() != null) {
            Plan newPlan = planRepository.findById(request.planId())
                    .orElseThrow(() -> new PlanNotFoundException(request.planId()));
            sub.setPlan(newPlan);
            sub.setEndDate(sub.getStartDate().plusDays(newPlan.getDurationInDays()));
            log.debug("Plan updated to {} for user={}", newPlan.getName(), userId);
        }

        if (request.tierId() != null) {
            Tier newTier = tierRepository.findById(request.tierId())
                    .orElseThrow(() -> new TierNotFoundException(request.tierId()));
            sub.setTier(newTier);
            log.debug("Tier updated to {} for user={}", newTier.getName(), userId);
        }

        Subscription saved = subscriptionRepository.save(sub);
        log.info("Subscription {} modified for user={}", saved.getId(), userId);
        return SubscriptionResponse.from(saved);
    }

    /**
     * System-driven tier upgrade (called by {@link TierEvaluationService}).
     * Only upgrades — never downgrades.
     */
    @Transactional
    public void upgradeTier(String userId, Tier newTier) {
        subscriptionRepository.findByUserIdAndStatus(userId, SubscriptionStatus.ACTIVE)
                .ifPresent(sub -> {
                    if (newTier.getLevel().isHigherThan(sub.getTier().getLevel())) {
                        log.info("System upgrading tier for user={} from {} to {}",
                                userId, sub.getTier().getLevel(), newTier.getLevel());
                        sub.setTier(newTier);
                        subscriptionRepository.save(sub);
                    } else {
                        log.debug("No upgrade needed for user={}: current={}, evaluated={}",
                                userId, sub.getTier().getLevel(), newTier.getLevel());
                    }
                });
    }

    private Subscription findActiveSubscription(String userId) {
        return subscriptionRepository.findByUserIdAndStatus(userId, SubscriptionStatus.ACTIVE)
                .orElseThrow(() -> new SubscriptionNotFoundException(userId));
    }
}
