package com.firstclub.membership.service;

import com.firstclub.membership.dto.response.CatalogResponse;
import com.firstclub.membership.dto.response.PlanResponse;
import com.firstclub.membership.dto.response.TierResponse;
import com.firstclub.membership.repository.PlanRepository;
import com.firstclub.membership.repository.TierRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@Transactional(readOnly = true)
public class CatalogService {

    private static final Logger log = LoggerFactory.getLogger(CatalogService.class);

    private final PlanRepository planRepository;
    private final TierRepository tierRepository;

    public CatalogService(PlanRepository planRepository, TierRepository tierRepository) {
        this.planRepository = planRepository;
        this.tierRepository = tierRepository;
    }

    public CatalogResponse getCatalog() {
        log.info("Fetching full membership catalog");
        List<PlanResponse> plans = planRepository.findAll()
                .stream()
                .map(PlanResponse::from)
                .toList();

        List<TierResponse> tiers = tierRepository.findAll()
                .stream()
                .map(TierResponse::from)
                .toList();

        log.debug("Catalog returned: {} plans, {} tiers", plans.size(), tiers.size());
        return new CatalogResponse(plans, tiers);
    }
}
