package com.ecommerce.backend.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

import com.ecommerce.backend.entity.Promotion;


public interface PromotionRepository extends JpaRepository<Promotion, Long> {
    List<Promotion> findByEnabledTrueOrderByStartsAtAsc();

    List<Promotion> findByEnabledTrueAndStartsAtLessThanEqualAndEndsAtGreaterThan(
        java.time.OffsetDateTime nowForStart,
        java.time.OffsetDateTime nowForEnd
    );
}
