package com.ecommerce.backend.dto;

import java.math.BigDecimal;

public record OrderItemResponse(
    Long id,
    Long productId,
    String productName,
    Integer quantity,
    BigDecimal unitPrice,
    BigDecimal subtotal,
    BigDecimal discountTotal,
    BigDecimal total
) {}
