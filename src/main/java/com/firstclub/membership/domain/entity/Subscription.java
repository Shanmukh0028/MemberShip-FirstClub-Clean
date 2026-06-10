package com.firstclub.membership.domain.entity;

import com.firstclub.membership.domain.enums.SubscriptionStatus;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * Tracks an active user membership. The {@code @Version} field enables
 * JPA Optimistic Locking: concurrent writes (e.g. user cancel + system upgrade)
 * are detected and the loser receives an ObjectOptimisticLockingFailureException,
 * which the global exception handler maps to HTTP 409 Conflict.
 */
@Entity
@Table(name = "subscriptions")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Subscription {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String userId;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "plan_id", nullable = false)
    private Plan plan;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "tier_id", nullable = false)
    private Tier tier;

    @Column(nullable = false)
    private LocalDateTime startDate;

    @Column(nullable = false)
    private LocalDateTime endDate;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private SubscriptionStatus status;

    /**
     * Non-null (= userId) while the subscription is ACTIVE; cleared to NULL
     * when cancelled or expired. A UNIQUE index on this column (see V3 migration)
     * prevents concurrent subscribe requests from creating two ACTIVE rows for the
     * same user, eliminating the TOCTOU race between existsBy-check and save().
     */
    @Column(unique = true)
    private String activeToken;

    /**
     * Optimistic lock version — incremented by JPA on every successful update.
     * Guards against lost-update race conditions on concurrent modifications
     * (e.g. user cancel vs. system tier upgrade).
     */
    @Version
    @Column(nullable = false)
    private Long version;
}
