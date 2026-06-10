package com.firstclub.membership.dto.benefit;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Configuration consumed by the Catalog / Marketing service to determine
 * early-access windows and coupon entitlements.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@JsonIgnoreProperties(ignoreUnknown = true)
public class ExclusiveDealsBenefitConfig extends BenefitConfig {

    private Integer earlyAccessHours;
    private Integer exclusiveCouponCount;

    @Override
    public String getType() {
        return "EXCLUSIVE_DEALS";
    }
}
