package com.firstclub.membership.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.concurrent.ConcurrentHashMap;

/**
 * In-memory simulation of the Order Service's monthly-spend API.
 */
@Service
public class MockOrderService implements OrderService {

    private static final Logger log = LoggerFactory.getLogger(MockOrderService.class);

    private final ConcurrentHashMap<String, BigDecimal> monthlySpend = new ConcurrentHashMap<>();

    @Override
    public void recordOrder(String userId, BigDecimal orderValue) {
        BigDecimal newTotal = monthlySpend.merge(userId, orderValue, BigDecimal::add);
        log.debug("Recorded order for userId={}: +{} → monthly total={}", userId, orderValue, newTotal);
    }

    @Override
    public BigDecimal getMonthlySpend(String userId) {
        return monthlySpend.getOrDefault(userId, BigDecimal.ZERO);
    }
}
