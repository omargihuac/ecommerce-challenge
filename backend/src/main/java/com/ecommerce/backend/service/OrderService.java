
package com.ecommerce.backend.service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.OffsetDateTime;
import java.util.List;
import java.util.concurrent.ThreadLocalRandom;
import java.util.stream.Collectors;

import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.ecommerce.backend.dto.OrderItemResponse;
import com.ecommerce.backend.dto.PurchaseOrderResponse;
import com.ecommerce.backend.entity.OrderItem;
import com.ecommerce.backend.entity.OrderItemDiscount;
import com.ecommerce.backend.entity.Product;
import com.ecommerce.backend.entity.Promotion;
import com.ecommerce.backend.entity.PurchaseOrder;
import com.ecommerce.backend.entity.User;
import com.ecommerce.backend.repository.ProductRepository;
import com.ecommerce.backend.repository.PromotionRepository;
import com.ecommerce.backend.repository.PurchaseOrderRepository;
import com.ecommerce.backend.repository.UserRepository;

@Service
public class OrderService {

    private final PurchaseOrderRepository orderRepository;
    private final ProductRepository productRepository;
    private final UserRepository userRepository;
    private final PromotionRepository promotionRepository;
    
    public OrderService(
            PurchaseOrderRepository orderRepository,
            ProductRepository productRepository,
            UserRepository userRepository,
            PromotionRepository promotionRepository) {
        this.orderRepository = orderRepository;
        this.productRepository = productRepository;
        this.userRepository = userRepository;
        this.promotionRepository = promotionRepository;
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
        
        List<Promotion> activePromotions = getActivePromotions();

        BigDecimal mainDiscountPercentage =
            getMainDiscountPercentage(activePromotions);

        boolean randomSelection =
            !activePromotions.isEmpty()
                && mainDiscountPercentage.compareTo(new BigDecimal("50")) == 0;

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
            
            
            BigDecimal itemDiscount = BigDecimal.ZERO;

            if (mainDiscountPercentage.compareTo(BigDecimal.ZERO) > 0) {
                OrderItemDiscount.DiscountType discountType = randomSelection
                    ? OrderItemDiscount.DiscountType.RANDOM_SELECTION
                    : OrderItemDiscount.DiscountType.PROMOTION;

                String description = randomSelection
                    ? "Descuento por selección aleatoria"
                    : "Descuento por promoción vigente";

                itemDiscount = applyDiscount(
                    item,
                    discountType,
                    mainDiscountPercentage,
                    description
                );
            }


            if (mainDiscountPercentage.compareTo(BigDecimal.ZERO) > 0) {
                itemDiscount = applyDiscount(
                    item,
                    OrderItemDiscount.DiscountType.PROMOTION,
                    mainDiscountPercentage,
                    "Descuento por promoción vigente"
                );
            }

            item.setDiscountTotal(itemDiscount);
            item.setTotal(itemSubtotal.subtract(itemDiscount));

            order.addItem(item);
            subtotal = subtotal.add(itemSubtotal);
        }

        BigDecimal totalDiscount = order.getItems().stream()
            .map(OrderItem::getDiscountTotal)
            .reduce(BigDecimal.ZERO, BigDecimal::add);

        order.setSubtotal(subtotal);
        order.setDiscountTotal(totalDiscount);
        order.setTotal(subtotal.subtract(totalDiscount));

        return orderRepository.save(order);
    }

    public record OrderItemRequest(
        Long productId,
        Integer quantity
    ) {}

    private List<Promotion> getActivePromotions() {
        OffsetDateTime now = OffsetDateTime.now();

        return promotionRepository
            .findByEnabledTrueAndStartsAtLessThanEqualAndEndsAtGreaterThan(
                now,
                now
            );
    }
    
    private boolean isRandomSelection() {
        return ThreadLocalRandom.current().nextBoolean();
    }
    
    private BigDecimal getMainDiscountPercentage(List<Promotion> promotions) {
        if (promotions == null || promotions.isEmpty()) {
            return BigDecimal.ZERO;
        }

        if (isRandomSelection()) {
            return new BigDecimal("50");
        }

        return getRegularDiscountPercentage(promotions);
    }

    private BigDecimal getRegularDiscountPercentage(List<Promotion> promotions) {
        return promotions.stream()
            .map(Promotion::getDiscountPercentage)
            .filter(percentage -> percentage != null)
            .max(BigDecimal::compareTo)
            .orElse(BigDecimal.ZERO);
    }

    /** Metodo que permite calcular y registrar un descuento sobre un 
     * articulo del pedido realizado
    */
    private BigDecimal applyDiscount(
            OrderItem item,
            OrderItemDiscount.DiscountType type,
            BigDecimal percentage,
            String description) {

        BigDecimal amount = item.getSubtotal()
            .multiply(percentage)
            .divide(new BigDecimal("100"), 2, RoundingMode.HALF_UP);

        if (amount.compareTo(BigDecimal.ZERO) <= 0) {
            return BigDecimal.ZERO;
        }

        OrderItemDiscount discount = OrderItemDiscount.create(
            type,
            percentage,
            amount,
            description
        );

        item.addDiscount(discount);

        return amount;
    }


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
