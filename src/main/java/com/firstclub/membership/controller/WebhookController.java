package com.firstclub.membership.controller;

import com.firstclub.membership.dto.request.OrderCompletedEvent;
import com.firstclub.membership.dto.response.ApiResponse;
import com.firstclub.membership.service.TierEvaluationService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * Simulates consuming an async Kafka {@code OrderCompletedEvent}.
 */
@RestController
@RequestMapping("/api/v1/webhooks")
@Tag(name = "Webhooks", description = "Simulates async Kafka OrderCompletedEvent to trigger the Tier Evaluation Engine")
public class WebhookController {

    private final TierEvaluationService tierEvaluationService;

    public WebhookController(TierEvaluationService tierEvaluationService) {
        this.tierEvaluationService = tierEvaluationService;
    }

    @PostMapping("/orders")
    @Operation(
            summary = "Order completed event (Kafka simulation)",
            description = """
                    Receives an order-completed event and runs the Tier Evaluation Engine.
                    Strategies evaluated: OrderCount, OrderValue, Cohort.
                    The highest qualifying tier is applied if higher than the user's current tier.
                    
                    Example cohort test: use userId starting with `vip_` or `platinum_` for instant PLATINUM upgrade.
                    """
    )
    public ResponseEntity<ApiResponse<Void>> handleOrderCompleted(
            @Valid @RequestBody OrderCompletedEvent event) {
        tierEvaluationService.evaluate(event);
        return ResponseEntity.ok(ApiResponse.ok("Order event processed. Tier evaluation complete.", null));
    }
}
