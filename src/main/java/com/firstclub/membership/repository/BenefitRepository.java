package com.firstclub.membership.repository;

import com.firstclub.membership.domain.entity.Benefit;
import com.firstclub.membership.domain.enums.BenefitType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface BenefitRepository extends JpaRepository<Benefit, Long> {

    List<Benefit> findByTierId(Long tierId);

    List<Benefit> findByTierIdAndBenefitType(Long tierId, BenefitType benefitType);
}
