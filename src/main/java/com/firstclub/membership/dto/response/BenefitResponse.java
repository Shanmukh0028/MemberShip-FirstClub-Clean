package com.firstclub.membership.dto.response;

import com.firstclub.membership.domain.enums.BenefitType;
import com.firstclub.membership.dto.benefit.BenefitConfig;

/**
 * Returned by the internal benefits API. The {@code config} field is
 * serialized as a polymorphic JSON object (with the {@code type} discriminator),
 * allowing consuming services to deserialize it directly into the correct
 * typed config class without any conditional logic.
 */
public record BenefitResponse(
        Long id,
        BenefitType benefitType,
        BenefitConfig config,
        String description
) {}
