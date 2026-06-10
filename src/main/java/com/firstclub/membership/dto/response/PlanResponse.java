package com.firstclub.membership.dto.response;

import com.firstclub.membership.domain.entity.Plan;
import com.firstclub.membership.domain.enums.PlanDuration;

import java.math.BigDecimal;

public record PlanResponse(
        Long id,
        String name,
        PlanDuration duration,
        Integer durationInDays,
        BigDecimal price
) {
    public static PlanResponse from(Plan plan) {
        return new PlanResponse(
                plan.getId(),
                plan.getName(),
                plan.getDuration(),
                plan.getDurationInDays(),
                plan.getPrice()
        );
    }
}
