package com.firstclub.membership.dto.benefit;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Configuration consumed by the Support service to route tickets
 * and set SLA expectations for premium members.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@JsonIgnoreProperties(ignoreUnknown = true)
public class SupportBenefitConfig extends BenefitConfig {

    private String priorityLevel;
    private Integer responseTimeSlaHours;
    private Boolean dedicatedManagerAssigned;

    @Override
    public String getType() {
        return "SUPPORT";
    }
}
