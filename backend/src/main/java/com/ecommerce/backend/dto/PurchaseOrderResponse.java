package com.ecommerce.backend.dto;

import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.util.List;

import com.ecommerce.backend.entity.PurchaseOrder;

public record PurchaseOrderResponse(
    Long id,
    PurchaseOrder.Status status,
    BigDecimal subtotal,
    BigDecimal discountTotal,
    BigDecimal total,
    OffsetDateTime createdAt,
    List<OrderItemResponse> items
) {}
