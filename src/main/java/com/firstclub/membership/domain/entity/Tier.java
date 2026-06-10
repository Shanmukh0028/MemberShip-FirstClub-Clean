package com.firstclub.membership.domain.entity;

import com.firstclub.membership.domain.enums.TierLevel;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "tiers")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Tier {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String name;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private TierLevel level;

    /**
     * Higher rank = more privileges (Silver=1, Gold=2, Platinum=3).
     */
    @Column(nullable = false)
    private Integer rankOrder;
}
