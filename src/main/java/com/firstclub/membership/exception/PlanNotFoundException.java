package com.firstclub.membership.exception;

public class PlanNotFoundException extends RuntimeException {

    public PlanNotFoundException(Long id) {
        super("Membership plan not found with id: " + id);
    }
}
