package com.firstclub.membership.strategy;

import com.firstclub.membership.domain.enums.TierLevel;
import com.firstclub.membership.dto.request.OrderCompletedEvent;
import com.firstclub.membership.service.OrderService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class TierEvaluationStrategyTest {

    // OrderCountStrategy and CohortStrategy have no dependencies
    private final OrderCountStrategy orderCountStrategy = new OrderCountStrategy();
    private final CohortStrategy cohortStrategy = new CohortStrategy();

    // OrderValueStrategy requires OrderService (uses monthly aggregate, not single event)
    @Mock private OrderService orderService;
    private OrderValueStrategy orderValueStrategy;

    @BeforeEach
    void setUp() {
        orderValueStrategy = new OrderValueStrategy(orderService);
    }

    // ─── OrderCountStrategy ───────────────────────────────────────────────────

    @ParameterizedTest(name = "orderCount={0} → {1}")
    @CsvSource({
            "50,  PLATINUM",
            "51,  PLATINUM",
            "10,  GOLD",
            "25,  GOLD",
            "3,   SILVER",
            "9,   SILVER",
            "2,   "
    })
    void orderCountStrategy_returnsExpectedTier(int count, String expectedLevel) {
        OrderCompletedEvent event = event("user1", new BigDecimal("100"), count);
        Optional<TierLevel> result = orderCountStrategy.evaluate(event);

        if (expectedLevel == null || expectedLevel.isBlank()) {
            assertThat(result).isEmpty();
        } else {
            assertThat(result).contains(TierLevel.valueOf(expectedLevel));
        }
    }

    // ─── OrderValueStrategy (monthly aggregate) ───────────────────────────────

    /**
     * Test OrderValueStrategy evaluates using monthly aggregate.
     */
    @ParameterizedTest(name = "monthlySpend={0} → {1}")
    @CsvSource({
            "20000, PLATINUM",
            "25000, PLATINUM",
            "5000,  GOLD",
            "10000, GOLD",
            "1000,  SILVER",
            "4999,  SILVER",
            "999,   "
    })
    void orderValueStrategy_usesMonthlyAggregate(String monthlySpend, String expectedLevel) {
        // The event's own orderValue is deliberately small — threshold evaluation
        // must NOT use it; it must call orderService.getMonthlySpend() instead.
        OrderCompletedEvent event = event("user1", new BigDecimal("1"), 1);
        when(orderService.getMonthlySpend("user1")).thenReturn(new BigDecimal(monthlySpend));

        Optional<TierLevel> result = orderValueStrategy.evaluate(event);

        if (expectedLevel == null || expectedLevel.isBlank()) {
            assertThat(result).isEmpty();
        } else {
            assertThat(result).contains(TierLevel.valueOf(expectedLevel));
        }
    }

    @Test
    void orderValueStrategy_ignoresSingleEventValue_usesMonthlyAggregate() {
        // User spent ₹30,000 total this month (across multiple orders),
        // but this specific event is only ₹500. Should still be PLATINUM.
        OrderCompletedEvent event = event("user1", new BigDecimal("500"), 5);
        when(orderService.getMonthlySpend("user1")).thenReturn(new BigDecimal("30000"));

        assertThat(orderValueStrategy.evaluate(event)).contains(TierLevel.PLATINUM);
    }

    // ─── CohortStrategy ───────────────────────────────────────────────────────

    @Test
    void cohortStrategy_vipPrefix_returnsPlatinum() {
        assertThat(cohortStrategy.evaluate(event("vip_user123", BigDecimal.ONE, 0)))
                .contains(TierLevel.PLATINUM);
    }

    @Test
    void cohortStrategy_platinumPrefix_returnsPlatinum() {
        assertThat(cohortStrategy.evaluate(event("platinum_vip", BigDecimal.ONE, 0)))
                .contains(TierLevel.PLATINUM);
    }

    @Test
    void cohortStrategy_goldPrefix_returnsGold() {
        assertThat(cohortStrategy.evaluate(event("gold_user", BigDecimal.ONE, 0)))
                .contains(TierLevel.GOLD);
    }

    @Test
    void cohortStrategy_premiumPrefix_returnsGold() {
        assertThat(cohortStrategy.evaluate(event("premium_member", BigDecimal.ONE, 0)))
                .contains(TierLevel.GOLD);
    }

    @Test
    void cohortStrategy_regularUser_returnsEmpty() {
        assertThat(cohortStrategy.evaluate(event("regular_user123", BigDecimal.ONE, 0)))
                .isEmpty();
    }

    @Test
    void cohortStrategy_isCaseInsensitive() {
        assertThat(cohortStrategy.evaluate(event("VIP_USER", BigDecimal.ONE, 0)))
                .contains(TierLevel.PLATINUM);
    }

    // ─── TierLevel rank ordering ──────────────────────────────────────────────

    @Test
    void tierLevel_rankOrdering_isCorrect() {
        assertThat(TierLevel.PLATINUM.isHigherThan(TierLevel.GOLD)).isTrue();
        assertThat(TierLevel.GOLD.isHigherThan(TierLevel.SILVER)).isTrue();
        assertThat(TierLevel.SILVER.isHigherThan(TierLevel.PLATINUM)).isFalse();
        assertThat(TierLevel.GOLD.isHigherThan(TierLevel.GOLD)).isFalse();
    }

    // ─── helpers ─────────────────────────────────────────────────────────────

    private OrderCompletedEvent event(String userId, BigDecimal orderValue, int orderCount) {
        return new OrderCompletedEvent(userId, "order-001", orderValue, orderCount);
    }
}
