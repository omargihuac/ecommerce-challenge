
package com.ecommerce.backend.service;

import java.math.BigDecimal;
import java.util.List;
import java.util.stream.Collectors;

import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.ecommerce.backend.dto.OrderItemResponse;
import com.ecommerce.backend.dto.PurchaseOrderResponse;
import com.ecommerce.backend.entity.OrderItem;
import com.ecommerce.backend.entity.Product;
import com.ecommerce.backend.entity.PurchaseOrder;
import com.ecommerce.backend.entity.User;
import com.ecommerce.backend.repository.ProductRepository;
import com.ecommerce.backend.repository.PurchaseOrderRepository;
import com.ecommerce.backend.repository.UserRepository;

@Service
public class OrderService {

    private final PurchaseOrderRepository orderRepository;
    private final ProductRepository productRepository;
    private final UserRepository userRepository;

    public OrderService(
            PurchaseOrderRepository orderRepository,
            ProductRepository productRepository,
            UserRepository userRepository) {
        this.orderRepository = orderRepository;
        this.productRepository = productRepository;
        this.userRepository = userRepository;
    }

    @Transactional
    public PurchaseOrder createOrder(
            Authentication authentication,
            List<OrderItemRequest> requestedItems) {

        if (requestedItems == null || requestedItems.isEmpty()) {
            throw new IllegalArgumentException(
                "El pedido debe contener al menos un producto"
            );
        }

        User customer = userRepository
            .findByEmailIgnoreCase(authentication.getName())
            .orElseThrow(() -> new IllegalArgumentException(
                "No se encontró el usuario autenticado"
            ));

        PurchaseOrder order = PurchaseOrder.create(customer);

        BigDecimal subtotal = BigDecimal.ZERO;

        for (OrderItemRequest requestedItem : requestedItems) {
            if (requestedItem.quantity() == null
                    || requestedItem.quantity() <= 0) {
                throw new IllegalArgumentException(
                    "La cantidad debe ser mayor que cero"
                );
            }

            Product product = productRepository
                .findById(requestedItem.productId())
                .filter(Product::isActive)
                .orElseThrow(() -> new IllegalArgumentException(
                    "Producto no encontrado o inactivo: "
                        + requestedItem.productId()
                ));

            if (product.getStockQuantity() < requestedItem.quantity()) {
                throw new IllegalArgumentException(
                    "Stock insuficiente para el producto: "
                        + product.getName()
                );
            }

            BigDecimal itemSubtotal = product.getPrice()
                .multiply(BigDecimal.valueOf(requestedItem.quantity()));

            OrderItem item = OrderItem.create();
            item.setProduct(product);
            item.setProductName(product.getName());
            item.setQuantity(requestedItem.quantity());
            item.setUnitPrice(product.getPrice());
            item.setSubtotal(itemSubtotal);
            item.setDiscountTotal(BigDecimal.ZERO);
            item.setTotal(itemSubtotal);

            order.addItem(item);
            subtotal = subtotal.add(itemSubtotal);
        }

        order.setSubtotal(subtotal);
        order.setDiscountTotal(BigDecimal.ZERO);
        order.setTotal(subtotal);

        return orderRepository.save(order);
    }

    public record OrderItemRequest(
        Long productId,
        Integer quantity
    ) {}

    public PurchaseOrderResponse toResponse(PurchaseOrder order) {
        List<OrderItemResponse> items = order.getItems().stream()
            .map(item -> new OrderItemResponse(
                item.getId(),
                item.getProduct().getId(),
                item.getProductName(),
                item.getQuantity(),
                item.getUnitPrice(),
                item.getSubtotal(),
                item.getDiscountTotal(),
                item.getTotal()
            ))
            .collect(Collectors.toList());

        return new PurchaseOrderResponse(
            order.getId(),
            order.getStatus(),
            order.getSubtotal(),
            order.getDiscountTotal(),
            order.getTotal(),
            order.getCreatedAt(),
            items
        );
    }
}
