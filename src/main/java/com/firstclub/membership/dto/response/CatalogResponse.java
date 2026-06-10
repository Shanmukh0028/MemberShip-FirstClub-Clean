package com.firstclub.membership.dto.response;

import java.util.List;

public record CatalogResponse(
        List<PlanResponse> plans,
        List<TierResponse> tiers
) {}
