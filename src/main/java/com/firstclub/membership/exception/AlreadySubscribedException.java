package com.firstclub.membership.exception;

public class AlreadySubscribedException extends RuntimeException {

    public AlreadySubscribedException(String userId) {
        super("User already has an active subscription: " + userId);
    }
}
