package com.ecommerce.backend.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

import com.ecommerce.backend.entity.OrderItemDiscount;

public interface OrderItemDiscountRepository extends JpaRepository<OrderItemDiscount, Long>{
        
     List<OrderItemDiscount> findByOrderItemId(Long orderItemId);
}
