package com.firstclub.membership.dto.response;

import java.math.BigDecimal;

/**
 * Minimum thresholds a user must satisfy (in any calendar month) to be
 * auto-upgraded to the containing tier by the Tier Evaluation Engine.
 *
 * <p>Both conditions are evaluated independently; meeting <em>either</em> one
 * is sufficient for the upgrade (OR semantics in {@code TierEvaluationService}).
 *
 * <p>Source of truth for the numeric values:
 * <ul>
 *   <li>{@code minOrderCount}  — {@code OrderCountStrategy} thresholds</li>
 *   <li>{@code minMonthlySpend} — {@code OrderValueStrategy} thresholds</li>
 * </ul>
 */
public record TierEligibilityCriteria(

        /**
         * Minimum cumulative order count required to reach this tier.
         */
        int minOrderCount,

        /**
         * Minimum total spend (in ₹) within a calendar month required to reach this tier.
         */
        BigDecimal minMonthlySpend
) {}
