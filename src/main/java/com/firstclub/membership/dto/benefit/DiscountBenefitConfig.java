package com.firstclub.membership.dto.benefit;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.util.List;

/**
 * Configuration consumed by the Pricing service to compute item-level discounts.
 * {@code applicableCategories} containing "ALL" means every category qualifies.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@JsonIgnoreProperties(ignoreUnknown = true)
public class DiscountBenefitConfig extends BenefitConfig {

    private BigDecimal discountPercentage;
    private BigDecimal maxDiscountAmount;
    private List<String> applicableCategories;

    @Override
    public String getType() {
        return "DISCOUNT";
    }
}
