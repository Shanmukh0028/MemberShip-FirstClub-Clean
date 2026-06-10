package com.firstclub.membership.service;

import com.firstclub.membership.domain.entity.Plan;
import com.firstclub.membership.domain.entity.Subscription;
import com.firstclub.membership.domain.entity.Tier;
import com.firstclub.membership.domain.enums.PlanDuration;
import com.firstclub.membership.domain.enums.SubscriptionStatus;
import com.firstclub.membership.domain.enums.TierLevel;
import com.firstclub.membership.dto.request.CreateSubscriptionRequest;
import com.firstclub.membership.dto.request.ModifySubscriptionRequest;
import com.firstclub.membership.dto.response.SubscriptionResponse;
import com.firstclub.membership.exception.AlreadySubscribedException;
import com.firstclub.membership.exception.SubscriptionNotFoundException;
import com.firstclub.membership.repository.PlanRepository;
import com.firstclub.membership.repository.SubscriptionRepository;
import com.firstclub.membership.repository.TierRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.dao.OptimisticLockingFailureException;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Optional;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class SubscriptionServiceTest {

    @Mock private SubscriptionRepository subscriptionRepository;
    @Mock private PlanRepository planRepository;
    @Mock private TierRepository tierRepository;

    @InjectMocks private SubscriptionService subscriptionService;

    private Plan monthlyPlan;
    private Tier silverTier;
    private Tier goldTier;

    @BeforeEach
    void setUp() {
        monthlyPlan = Plan.builder()
                .id(1L).name("Monthly Plan")
                .duration(PlanDuration.MONTHLY).durationInDays(30)
                .price(new BigDecimal("99.00")).build();

        silverTier = Tier.builder()
                .id(1L).name("Silver").level(TierLevel.SILVER).rankOrder(1).build();

        goldTier = Tier.builder()
                .id(2L).name("Gold").level(TierLevel.GOLD).rankOrder(2).build();
    }

    @Test
    void subscribe_createsNewSubscriptionSuccessfully() {
        when(subscriptionRepository.existsByUserIdAndStatus("user1", SubscriptionStatus.ACTIVE)).thenReturn(false);
        when(planRepository.findById(1L)).thenReturn(Optional.of(monthlyPlan));
        when(tierRepository.findById(1L)).thenReturn(Optional.of(silverTier));

        Subscription saved = buildSubscription("user1", monthlyPlan, silverTier, SubscriptionStatus.ACTIVE);
        when(subscriptionRepository.save(any())).thenReturn(saved);

        SubscriptionResponse response = subscriptionService.subscribe("user1",
                new CreateSubscriptionRequest(1L, 1L));

        assertThat(response.userId()).isEqualTo("user1");
        assertThat(response.status()).isEqualTo(SubscriptionStatus.ACTIVE);
        assertThat(response.tier().level()).isEqualTo(TierLevel.SILVER);
        verify(subscriptionRepository).save(any(Subscription.class));
    }

    @Test
    void subscribe_throwsAlreadySubscribed_whenActiveExists() {
        when(subscriptionRepository.existsByUserIdAndStatus("user1", SubscriptionStatus.ACTIVE)).thenReturn(true);

        assertThatThrownBy(() ->
                subscriptionService.subscribe("user1", new CreateSubscriptionRequest(1L, 1L)))
                .isInstanceOf(AlreadySubscribedException.class);

        verify(subscriptionRepository, never()).save(any());
    }

    @Test
    void cancel_setsStatusToCancelled() {
        Subscription active = buildSubscription("user1", monthlyPlan, silverTier, SubscriptionStatus.ACTIVE);
        when(subscriptionRepository.findByUserIdAndStatus("user1", SubscriptionStatus.ACTIVE))
                .thenReturn(Optional.of(active));

        Subscription cancelled = buildSubscription("user1", monthlyPlan, silverTier, SubscriptionStatus.CANCELLED);
        when(subscriptionRepository.save(any())).thenReturn(cancelled);

        SubscriptionResponse response = subscriptionService.cancel("user1");

        assertThat(response.status()).isEqualTo(SubscriptionStatus.CANCELLED);
    }

    /**
     * DataIntegrityViolationException (DB unique constraint on active_token)
     * must be caught and surfaced as AlreadySubscribedException → HTTP 409.
     */
    @Test
    void subscribe_throwsAlreadySubscribed_onDataIntegrityViolation() {
        when(subscriptionRepository.existsByUserIdAndStatus("user1", SubscriptionStatus.ACTIVE)).thenReturn(false);
        when(planRepository.findById(1L)).thenReturn(Optional.of(monthlyPlan));
        when(tierRepository.findById(1L)).thenReturn(Optional.of(silverTier));
        when(subscriptionRepository.save(any()))
                .thenThrow(new DataIntegrityViolationException("unique constraint: active_token"));

        assertThatThrownBy(() ->
                subscriptionService.subscribe("user1", new CreateSubscriptionRequest(1L, 1L)))
                .isInstanceOf(AlreadySubscribedException.class);
    }

    @Test
    void cancel_throwsNotFound_whenNoActiveSubscription() {
        when(subscriptionRepository.findByUserIdAndStatus("user1", SubscriptionStatus.ACTIVE))
                .thenReturn(Optional.empty());

        assertThatThrownBy(() -> subscriptionService.cancel("user1"))
                .isInstanceOf(SubscriptionNotFoundException.class);
    }

    @Test
    void modify_upgradesTier() {
        Subscription active = buildSubscription("user1", monthlyPlan, silverTier, SubscriptionStatus.ACTIVE);
        when(subscriptionRepository.findByUserIdAndStatus("user1", SubscriptionStatus.ACTIVE))
                .thenReturn(Optional.of(active));
        when(tierRepository.findById(2L)).thenReturn(Optional.of(goldTier));

        Subscription upgraded = buildSubscription("user1", monthlyPlan, goldTier, SubscriptionStatus.ACTIVE);
        when(subscriptionRepository.save(any())).thenReturn(upgraded);

        SubscriptionResponse response = subscriptionService.modify("user1",
                new ModifySubscriptionRequest(null, 2L));

        assertThat(response.tier().level()).isEqualTo(TierLevel.GOLD);
    }

    @Test
    void modify_throwsIllegalArgument_whenBothIdsNull() {
        assertThatThrownBy(() ->
                subscriptionService.modify("user1", new ModifySubscriptionRequest(null, null)))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("At least one");
    }

    /**
     * Cancel (autoRenew=false) is still guarded by @Version optimistic locking.
     * The exception must propagate to the global handler → HTTP 409.
     */
    @Test
    void cancel_propagatesOptimisticLockingFailure() {
        Subscription active = buildSubscription("user1", monthlyPlan, silverTier, SubscriptionStatus.ACTIVE);
        when(subscriptionRepository.findByUserIdAndStatus("user1", SubscriptionStatus.ACTIVE))
                .thenReturn(Optional.of(active));
        when(subscriptionRepository.save(any()))
                .thenThrow(new OptimisticLockingFailureException("version conflict"));

        assertThatThrownBy(() -> subscriptionService.cancel("user1"))
                .isInstanceOf(OptimisticLockingFailureException.class);
    }

    @Test
    void upgradeTier_doesNotDowngrade() {
        Subscription active = buildSubscription("user1", monthlyPlan, goldTier, SubscriptionStatus.ACTIVE);
        when(subscriptionRepository.findByUserIdAndStatus("user1", SubscriptionStatus.ACTIVE))
                .thenReturn(Optional.of(active));

        subscriptionService.upgradeTier("user1", silverTier);

        verify(subscriptionRepository, never()).save(any());
    }

    private Subscription buildSubscription(String userId, Plan plan, Tier tier, SubscriptionStatus status) {
        return Subscription.builder()
                .id(1L).userId(userId).plan(plan).tier(tier)
                .startDate(LocalDateTime.now())
                .endDate(LocalDateTime.now().plusDays(plan.getDurationInDays()))
                .status(status)
                .activeToken(status == SubscriptionStatus.ACTIVE ? userId : null)
                .version(0L).build();
    }
}
