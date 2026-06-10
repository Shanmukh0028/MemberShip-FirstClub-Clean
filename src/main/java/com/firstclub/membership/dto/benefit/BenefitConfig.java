package com.firstclub.membership.dto.benefit;

import com.fasterxml.jackson.annotation.JsonSubTypes;
import com.fasterxml.jackson.annotation.JsonTypeInfo;

/**
 * Polymorphic root for all benefit configurations.
 * 
 * The {@code type} property in the JSON drives Jackson's deserialization to
 * the correct concrete subtype.
 */
@JsonTypeInfo(use = JsonTypeInfo.Id.NAME, property = "type")
@JsonSubTypes({
        @JsonSubTypes.Type(value = ShippingBenefitConfig.class,      name = "SHIPPING"),
        @JsonSubTypes.Type(value = DiscountBenefitConfig.class,      name = "DISCOUNT"),
        @JsonSubTypes.Type(value = ExclusiveDealsBenefitConfig.class, name = "EXCLUSIVE_DEALS"),
        @JsonSubTypes.Type(value = SupportBenefitConfig.class,       name = "SUPPORT")
})
public abstract class BenefitConfig {

    public abstract String getType();
}
