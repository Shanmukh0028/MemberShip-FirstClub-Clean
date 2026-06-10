package com.firstclub.membership.dto.benefit;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.util.List;

/**
 * Configuration consumed by the Checkout / Logistics service to apply
 * free-delivery rules. Threshold=0 means free on all orders.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@JsonIgnoreProperties(ignoreUnknown = true)
public class ShippingBenefitConfig extends BenefitConfig {

    private BigDecimal freeDeliveryThreshold;
    private BigDecimal maxDeliveryDiscount;
    private List<String> eligibleCategories;

    @Override
    public String getType() {
        return "SHIPPING";
    }
}
