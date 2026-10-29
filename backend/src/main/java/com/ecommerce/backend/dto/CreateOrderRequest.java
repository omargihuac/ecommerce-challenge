package com.ecommerce.backend.dto;

import java.util.List;

import com.ecommerce.backend.service.OrderService.OrderItemRequest;

public record CreateOrderRequest(
    List<OrderItemRequest> items
) {}
