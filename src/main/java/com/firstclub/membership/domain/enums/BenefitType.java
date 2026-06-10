package com.firstclub.membership.domain.enums;

/**
 * Types of benefits supported by the membership system.
 * Each type maps to a distinct polymorphic BenefitConfig subtype.
 */
public enum BenefitType {
    SHIPPING,
    DISCOUNT,
    EXCLUSIVE_DEALS,
    SUPPORT
}
