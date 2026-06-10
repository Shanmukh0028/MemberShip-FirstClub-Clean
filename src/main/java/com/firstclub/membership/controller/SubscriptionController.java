package com.firstclub.membership.controller;

import com.firstclub.membership.dto.request.CreateSubscriptionRequest;
import com.firstclub.membership.dto.request.ModifySubscriptionRequest;
import com.firstclub.membership.dto.response.ApiResponse;
import com.firstclub.membership.dto.response.SubscriptionResponse;
import com.firstclub.membership.service.SubscriptionService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/subscriptions")
@Tag(name = "Subscriptions", description = "User subscription lifecycle management")
public class SubscriptionController {

    private final SubscriptionService subscriptionService;

    public SubscriptionController(SubscriptionService subscriptionService) {
        this.subscriptionService = subscriptionService;
    }

    @GetMapping("/me")
    @Operation(
            summary = "Get current subscription",
            description = "Returns the active subscription for the requesting user"
    )
    public ResponseEntity<ApiResponse<SubscriptionResponse>> getSubscription(
            @Parameter(description = "User identifier", required = true)
            @RequestHeader("X-User-Id") String userId) {
        return ResponseEntity.ok(ApiResponse.ok(subscriptionService.getSubscription(userId)));
    }

    @PostMapping
    @Operation(
            summary = "Subscribe to a plan",
            description = "Creates a new membership subscription for the user. One active subscription allowed per user."
    )
    public ResponseEntity<ApiResponse<SubscriptionResponse>> subscribe(
            @RequestHeader("X-User-Id") String userId,
            @Valid @RequestBody CreateSubscriptionRequest request) {
        SubscriptionResponse response = subscriptionService.subscribe(userId, request);
        return ResponseEntity.status(HttpStatus.CREATED).body(ApiResponse.ok("Subscription created successfully", response));
    }

    @PutMapping("/me/cancel")
    @Operation(
            summary = "Cancel subscription",
            description = "Cancels the active subscription. The subscription remains valid until endDate."
    )
    public ResponseEntity<ApiResponse<SubscriptionResponse>> cancel(
            @RequestHeader("X-User-Id") String userId) {
        return ResponseEntity.ok(ApiResponse.ok("Subscription cancelled successfully", subscriptionService.cancel(userId)));
    }

    @PutMapping("/me/modify")
    @Operation(
            summary = "Upgrade or downgrade subscription",
            description = "Modifies the active subscription's plan and/or tier. Provide at least one of planId or tierId."
    )
    public ResponseEntity<ApiResponse<SubscriptionResponse>> modify(
            @RequestHeader("X-User-Id") String userId,
            @Valid @RequestBody ModifySubscriptionRequest request) {
        return ResponseEntity.ok(ApiResponse.ok("Subscription modified successfully", subscriptionService.modify(userId, request)));
    }
}
