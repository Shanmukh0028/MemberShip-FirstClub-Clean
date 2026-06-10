package com.firstclub.membership.dto.request;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

public record CreateSubscriptionRequest(

        @NotNull(message = "planId is required")
        @Positive(message = "planId must be a positive number")
        Long planId,

        @NotNull(message = "tierId is required")
        @Positive(message = "tierId must be a positive number")
        Long tierId
) {}
