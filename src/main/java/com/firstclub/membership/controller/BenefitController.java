package com.firstclub.membership.controller;

import com.firstclub.membership.domain.enums.BenefitType;
import com.firstclub.membership.dto.response.ApiResponse;
import com.firstclub.membership.dto.response.BenefitResponse;
import com.firstclub.membership.service.BenefitService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * Internal API — intended for consumption by other micro-services (Checkout, Pricing, Support).
 * Not exposed to end-users; no authentication is simulated here for demo purposes.
 */
@RestController
@RequestMapping("/internal/v1/users")
@Tag(name = "Internal Benefits API", description = "Service-to-service: fetch typed benefit configurations for a user")
public class BenefitController {

    private final BenefitService benefitService;

    public BenefitController(BenefitService benefitService) {
        this.benefitService = benefitService;
    }

    @GetMapping("/{userId}/benefits")
    @Operation(
            summary = "Get user benefits",
            description = """
                    Returns strongly-typed benefit configurations for the user's current tier.
                    
                    Use the optional **type** filter for data minimization:
                    - Checkout Service → `?type=SHIPPING`
                    - Pricing Service  → `?type=DISCOUNT`
                    - Support Service  → `?type=SUPPORT`
                    - Catalog Service  → `?type=EXCLUSIVE_DEALS`
                    
                    Omit **type** to receive all benefit types.
                    """
    )
    public ResponseEntity<ApiResponse<List<BenefitResponse>>> getBenefits(
            @Parameter(description = "Target user identifier", required = true)
            @PathVariable String userId,

            @Parameter(description = "Filter by benefit type (SHIPPING, DISCOUNT, EXCLUSIVE_DEALS, SUPPORT)")
            @RequestParam(required = false) BenefitType type) {

        return ResponseEntity.ok(ApiResponse.ok(benefitService.getBenefits(userId, type)));
    }
}
