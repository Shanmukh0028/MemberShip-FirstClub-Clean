package com.firstclub.membership.repository;

import com.firstclub.membership.domain.entity.Subscription;
import com.firstclub.membership.domain.enums.SubscriptionStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface SubscriptionRepository extends JpaRepository<Subscription, Long> {

    Optional<Subscription> findByUserIdAndStatus(String userId, SubscriptionStatus status);

    boolean existsByUserIdAndStatus(String userId, SubscriptionStatus status);
}
