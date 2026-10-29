
package com.ecommerce.backend.controller;

import java.util.List;

import org.springframework.http.HttpStatus;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import com.ecommerce.backend.dto.CreateOrderRequest;
import com.ecommerce.backend.dto.PurchaseOrderResponse;
import com.ecommerce.backend.entity.PurchaseOrder;
import com.ecommerce.backend.service.OrderService;

@RestController
@RequestMapping("/api/orders")
public class OrderController {

    private final OrderService orderService;

    public OrderController(OrderService orderService) {
        this.orderService = orderService;
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public PurchaseOrderResponse createOrder(
            Authentication authentication,
            @RequestBody CreateOrderRequest request) {

        PurchaseOrder order = orderService.createOrder(
            authentication,
            request.items(),
            Boolean.TRUE.equals(request.randomSelection())
        );

        return orderService.toResponse(order);
    }

    @PatchMapping("/{id}/complete")
    public PurchaseOrderResponse completeOrder(@PathVariable Long id) {
        return orderService.completeOrder(id);
    }

    @GetMapping
    public List<PurchaseOrderResponse> getMyOrders(
            Authentication authentication) {
        return orderService.getMyOrders(authentication);
    }

}
