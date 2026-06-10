package com.firstclub.membership.domain.entity;

import com.firstclub.membership.domain.enums.PlanDuration;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Entity
@Table(name = "plans")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Plan {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String name;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private PlanDuration duration;

    @Column(nullable = false)
    private Integer durationInDays;

    @Column(nullable = false, precision = 10, scale = 2)
    private BigDecimal price;
}
