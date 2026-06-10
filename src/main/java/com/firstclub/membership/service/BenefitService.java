package com.firstclub.membership.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.firstclub.membership.domain.entity.Benefit;
import com.firstclub.membership.domain.enums.BenefitType;
import com.firstclub.membership.domain.enums.SubscriptionStatus;
import com.firstclub.membership.dto.benefit.BenefitConfig;
import com.firstclub.membership.dto.response.BenefitResponse;
import com.firstclub.membership.exception.SubscriptionNotFoundException;
import com.firstclub.membership.repository.BenefitRepository;
import com.firstclub.membership.repository.SubscriptionRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@Transactional(readOnly = true)
public class BenefitService {

    private static final Logger log = LoggerFactory.getLogger(BenefitService.class);

    private final BenefitRepository benefitRepository;
    private final SubscriptionRepository subscriptionRepository;
    private final ObjectMapper objectMapper;

    public BenefitService(BenefitRepository benefitRepository,
                          SubscriptionRepository subscriptionRepository,
                          ObjectMapper objectMapper) {
        this.benefitRepository = benefitRepository;
        this.subscriptionRepository = subscriptionRepository;
        this.objectMapper = objectMapper;
    }

    /**
     * Returns benefits for the user's current tier, optionally filtered by type.
     *
     * @param userId      the user to look up
     * @param benefitType optional filter; {@code null} returns all benefit types
     */
    public List<BenefitResponse> getBenefits(String userId, BenefitType benefitType) {
        log.info("Fetching benefits for userId={}, type={}", userId, benefitType);

        Long tierId = subscriptionRepository
                .findByUserIdAndStatus(userId, SubscriptionStatus.ACTIVE)
                .orElseThrow(() -> new SubscriptionNotFoundException(userId))
                .getTier()
                .getId();

        List<Benefit> benefits = (benefitType != null)
                ? benefitRepository.findByTierIdAndBenefitType(tierId, benefitType)
                : benefitRepository.findByTierId(tierId);

        log.debug("Found {} benefits for userId={}, tierId={}", benefits.size(), userId, tierId);

        return benefits.stream()
                .map(this::toResponse)
                .toList();
    }

    private BenefitResponse toResponse(Benefit benefit) {
        try {
            BenefitConfig config = objectMapper.readValue(benefit.getConfigJson(), BenefitConfig.class);
            return new BenefitResponse(
                    benefit.getId(),
                    benefit.getBenefitType(),
                    config,
                    benefit.getDescription()
            );
        } catch (JsonProcessingException e) {
            log.error("Failed to deserialize configJson for benefitId={}: {}", benefit.getId(), e.getMessage());
            throw new IllegalStateException("Corrupt benefit configuration for id=" + benefit.getId(), e);
        }
    }
}
