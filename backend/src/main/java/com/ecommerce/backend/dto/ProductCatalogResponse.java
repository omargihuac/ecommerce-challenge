
package com.ecommerce.backend.dto;

import java.math.BigDecimal;

public record ProductCatalogResponse(
    Long id,
    String name,
    String description,
    String sku,
    BigDecimal originalPrice,
    BigDecimal discountPercentage,
    BigDecimal finalPrice,
    Integer stockQuantity
) {}
