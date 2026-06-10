package com.firstclub.membership.dto.response;

import com.firstclub.membership.domain.entity.Subscription;
import com.firstclub.membership.domain.enums.SubscriptionStatus;

import java.time.LocalDateTime;

public record SubscriptionResponse(
        Long id,
        String userId,
        PlanResponse plan,
        TierResponse tier,
        LocalDateTime startDate,
        LocalDateTime endDate,
        SubscriptionStatus status,
        Long version
) {
    public static SubscriptionResponse from(Subscription sub) {
        return new SubscriptionResponse(
                sub.getId(),
                sub.getUserId(),
                PlanResponse.from(sub.getPlan()),
                TierResponse.from(sub.getTier()),
                sub.getStartDate(),
                sub.getEndDate(),
                sub.getStatus(),
                sub.getVersion()
        );
    }
}
