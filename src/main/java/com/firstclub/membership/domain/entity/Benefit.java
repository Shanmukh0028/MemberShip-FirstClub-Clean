package com.firstclub.membership.domain.entity;

import com.firstclub.membership.domain.enums.BenefitType;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Stores benefit configuration per tier as raw JSON.
 * The JSON is deserialized into a strongly-typed {@code BenefitConfig}
 * subtype by the service layer using Jackson polymorphism.
 */
@Entity
@Table(name = "benefits")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Benefit {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "tier_id", nullable = false)
    private Tier tier;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private BenefitType benefitType;

    /**
     * Raw JSON blob. The {@code type} discriminator field inside the JSON
     * drives Jackson's polymorphic deserialization to the correct config class.
     */
    @Column(columnDefinition = "TEXT", nullable = false)
    private String configJson;

    @Column
    private String description;
}
