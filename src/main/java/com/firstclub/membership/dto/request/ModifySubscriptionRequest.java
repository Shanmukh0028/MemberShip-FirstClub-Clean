package com.firstclub.membership.dto.request;

import jakarta.validation.constraints.Positive;

/**
 * At least one of planId / tierId must be non-null (validated at service layer).
 */
public record ModifySubscriptionRequest(

        @Positive(message = "planId must be a positive number")
        Long planId,

        @Positive(message = "tierId must be a positive number")
        Long tierId
) {}
