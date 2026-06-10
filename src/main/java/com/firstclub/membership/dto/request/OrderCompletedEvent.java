package com.firstclub.membership.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PositiveOrZero;

import java.math.BigDecimal;

/**
 * Simulates an {@code OrderCompletedEvent} that would normally arrive via Kafka.
 * The webhook endpoint consumes this payload and triggers the Tier Evaluation Engine.
 *
 * @param userId          the user who completed the order
 * @param orderId         the completed order identifier
 * @param orderValue      the monetary value of this specific order
 * @param totalOrderCount the user's cumulative order count (lifetime or windowed)
 */
public record OrderCompletedEvent(

        @NotBlank(message = "userId is required")
        String userId,

        @NotBlank(message = "orderId is required")
        String orderId,

        @NotNull(message = "orderValue is required")
        @PositiveOrZero(message = "orderValue must be >= 0")
        BigDecimal orderValue,

        @PositiveOrZero(message = "totalOrderCount must be >= 0")
        int totalOrderCount
) {}
