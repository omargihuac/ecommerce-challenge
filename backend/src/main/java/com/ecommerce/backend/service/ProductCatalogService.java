
package com.ecommerce.backend.service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.OffsetDateTime;
import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.ecommerce.backend.dto.ProductCatalogResponse;
import com.ecommerce.backend.entity.Product;
import com.ecommerce.backend.entity.Promotion;
import com.ecommerce.backend.repository.ProductRepository;
import com.ecommerce.backend.repository.PromotionRepository;

@Service
@Transactional(readOnly = true)
public class ProductCatalogService {

    private final ProductRepository productRepository;
    private final PromotionRepository promotionRepository;

    public ProductCatalogService(
            ProductRepository productRepository,
            PromotionRepository promotionRepository) {
        this.productRepository = productRepository;
        this.promotionRepository = promotionRepository;
    }

    public List<ProductCatalogResponse> findAll() {
        OffsetDateTime now = OffsetDateTime.now();

        List<Promotion> promotions =
            promotionRepository
                .findByEnabledTrueAndStartsAtLessThanEqualAndEndsAtGreaterThan(
                    now, now
                );

        BigDecimal discount = promotions.stream()
            .map(Promotion::getDiscountPercentage)
            .max(BigDecimal::compareTo)
            .orElse(BigDecimal.ZERO);

        return productRepository.findByActiveTrue().stream()
            .map(product -> toResponse(product, discount))
            .toList();
    }

    private ProductCatalogResponse toResponse(
            Product product,
            BigDecimal discount) {

        BigDecimal finalPrice = product.getPrice()
            .multiply(
                BigDecimal.ONE.subtract(
                    discount.divide(new BigDecimal("100"), 4,
                        RoundingMode.HALF_UP)
                )
            )
            .setScale(2, RoundingMode.HALF_UP);

        return new ProductCatalogResponse(
            product.getId(),
            product.getName(),
            product.getDescription(),
            product.getSku(),
            product.getPrice(),
            discount,
            finalPrice,
            product.getStockQuantity()
        );
    }
}
