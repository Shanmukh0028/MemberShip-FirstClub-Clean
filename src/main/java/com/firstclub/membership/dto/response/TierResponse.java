package com.firstclub.membership.dto.response;

import com.firstclub.membership.domain.entity.Tier;
import com.firstclub.membership.domain.enums.TierLevel;

import java.math.BigDecimal;

public record TierResponse(
        Long id,
        String name,
        TierLevel level,
        Integer rankOrder,

        /**
         * Minimum thresholds to be auto-upgraded to this tier.
         * Satisfying either condition (order count OR monthly spend) triggers the upgrade.
         */
        TierEligibilityCriteria eligibilityToUpgrade
) {
    public static TierResponse from(Tier tier) {
        return new TierResponse(
                tier.getId(),
                tier.getName(),
                tier.getLevel(),
                tier.getRankOrder(),
                criteriaFor(tier.getLevel())
        );
    }

    private static TierEligibilityCriteria criteriaFor(TierLevel level) {
        return switch (level) {
            case SILVER   -> new TierEligibilityCriteria(0,  BigDecimal.ZERO);
            case GOLD     -> new TierEligibilityCriteria(10, new BigDecimal("5000"));
            case PLATINUM -> new TierEligibilityCriteria(50, new BigDecimal("20000"));
        };
    }
}
