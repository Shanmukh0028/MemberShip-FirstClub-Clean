package com.firstclub.membership.repository;

import com.firstclub.membership.domain.entity.Tier;
import com.firstclub.membership.domain.enums.TierLevel;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface TierRepository extends JpaRepository<Tier, Long> {

    Optional<Tier> findByLevel(TierLevel level);
}
