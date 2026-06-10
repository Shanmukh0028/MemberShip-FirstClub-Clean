package com.firstclub.membership.exception;

import com.firstclub.membership.domain.enums.TierLevel;

public class TierNotFoundException extends RuntimeException {

    public TierNotFoundException(Long id) {
        super("Membership tier not found with id: " + id);
    }

    public TierNotFoundException(TierLevel level) {
        super("Membership tier not found for level: " + level);
    }
}
