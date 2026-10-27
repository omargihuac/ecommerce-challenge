package com.ecommerce.backend.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

import com.ecommerce.backend.entity.PurchaseOrder;

public interface PurchaseOrderRepository extends JpaRepository<PurchaseOrder, Long>{
    
    List<PurchaseOrder> findByCustomerIdOrderByCreatedAtDesc(Long customerId);

    List<PurchaseOrder> findByStatusOrderByCreatedAtDesc(
        PurchaseOrder.Status status
    );
}
