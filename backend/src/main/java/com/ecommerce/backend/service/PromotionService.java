
package com.ecommerce.backend.service;

import java.math.BigDecimal;
import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.ecommerce.backend.dto.PromotionRequest;
import com.ecommerce.backend.entity.Promotion;
import com.ecommerce.backend.repository.PromotionRepository;

@Service
@Transactional(readOnly = true)
public class PromotionService {

    private final PromotionRepository promotionRepository;

    public PromotionService(PromotionRepository promotionRepository) {
        this.promotionRepository = promotionRepository;
    }

    public List<Promotion> findEnabled() {
        return promotionRepository.findByEnabledTrueOrderByStartsAtAsc();
    }

    public Promotion findEnabledById(Long id) {
        return promotionRepository.findById(id)
            .filter(Promotion::isEnabled)
            .orElseThrow(() -> new PromotionNotFoundException(
                "No se encontró la promoción habilitada con ID: " + id
            ));
    }

    @Transactional
    public Promotion create(PromotionRequest request) {
        validatePromotionData(request);

        Promotion promotion = Promotion.create(
            request.name().trim(),
            request.description(),
            request.discountPercentage(),
            request.startsAt(),
            request.endsAt()
        );

        return promotionRepository.save(promotion);
    }

    @Transactional
    public Promotion update(Long id, PromotionRequest request) {
        validatePromotionData(request);

        Promotion promotion = findEnabledById(id);
        applyRequest(promotion, request);

        return promotionRepository.save(promotion);
    }

    @Transactional
    public void disable(Long id) {
        Promotion promotion = findEnabledById(id);
        promotion.setEnabled(false);
        promotionRepository.save(promotion);
    }

    private void validatePromotionData(PromotionRequest request) {
        if (request.name() == null || request.name().isBlank()) {
            throw new IllegalArgumentException(
                "El nombre de la promoción es obligatorio."
            );
        }

        BigDecimal discount = request.discountPercentage();

        if (discount == null
                || discount.compareTo(BigDecimal.ZERO) <= 0
                || discount.compareTo(new BigDecimal("100")) > 0) {
            throw new IllegalArgumentException(
                "El descuento debe ser mayor que cero y no superar el 100%."
            );
        }

        if (request.startsAt() == null || request.endsAt() == null
                || !request.endsAt().isAfter(request.startsAt())) {
            throw new IllegalArgumentException(
                "La fecha de finalización debe ser posterior a la fecha de inicio."
            );
        }
    }

    private void applyRequest(
            Promotion promotion,
            PromotionRequest request) {

        promotion.setName(request.name().trim());
        promotion.setDescription(request.description());
        promotion.setDiscountPercentage(request.discountPercentage());
        promotion.setStartsAt(request.startsAt());
        promotion.setEndsAt(request.endsAt());
    }
}
