package com.firstclub.membership.exception;

public class SubscriptionNotFoundException extends RuntimeException {

    public SubscriptionNotFoundException(String userId) {
        super("No active subscription found for user: " + userId);
    }

    public SubscriptionNotFoundException(Long id) {
        super("Subscription not found with id: " + id);
    }
}
