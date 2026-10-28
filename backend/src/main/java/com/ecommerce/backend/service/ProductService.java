package com.ecommerce.backend.service;

import java.math.BigDecimal;
import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.ecommerce.backend.dto.ProductRequest;
import com.ecommerce.backend.entity.Product;
import com.ecommerce.backend.repository.ProductRepository;

@Service
@Transactional(readOnly = true)
public class ProductService {

    private final ProductRepository productRepository;

    public ProductService(ProductRepository productRepository) {
        this.productRepository = productRepository;
    }

    public List<Product> findAllActive() {
        return productRepository.findByActiveTrueOrderByNameAsc();
    }

    public Product findActiveById(Long id) {
        return productRepository.findById(id)
            .filter(Product::isActive)
            .orElseThrow(() ->
                new ProductNotFoundException(
                    "No se encontró el producto con ID: " + id
                )
            );
    }

    @Transactional
    public Product create(ProductRequest request) {
        validateProductData(request);
        validateSkuIsAvailable(request.sku(), null);

        Product product = new Product(
            request.name().trim(),
            request.description(),
            request.sku().trim(),
            request.price(),
            request.stockQuantity()
        );

        return productRepository.save(product);
    }

    @Transactional
    public Product update(Long id, ProductRequest request) {
        validateProductData(request);

        Product product = findActiveById(id);
        validateSkuIsAvailable(request.sku(), id);

        applyRequest(product, request);

        return productRepository.save(product);
    }

    @Transactional
    public void deactivate(Long id) {
        Product product = findActiveById(id);
        product.setActive(false);
        productRepository.save(product);
    }

    private void validateProductData(ProductRequest request) {
        if (request.price() == null
                || request.price().compareTo(BigDecimal.ZERO) <= 0) {
            throw new IllegalArgumentException(
                "El precio debe ser mayor que cero."
            );
        }

        if (request.stockQuantity() == null
                || request.stockQuantity() < 0) {
            throw new IllegalArgumentException(
                "El stock no puede ser negativo."
            );
        }

        if (request.name() == null || request.name().isBlank()) {
            throw new IllegalArgumentException(
                "El nombre del producto es obligatorio."
            );
        }

        if (request.sku() == null || request.sku().isBlank()) {
            throw new IllegalArgumentException(
                "El SKU del producto es obligatorio."
            );
        }
    }

    private void validateSkuIsAvailable(String sku, Long currentProductId) {
        productRepository.findBySku(sku.trim())
            .filter(product ->
                currentProductId == null
                || !product.getId().equals(currentProductId))
            .ifPresent(product -> {
                throw new DuplicateProductSkuException(
                    "Ya existe un producto con el SKU: " + sku
                );
            });
    }

    private void applyRequest(Product product, ProductRequest request) {
        product.setName(request.name().trim());
        product.setDescription(request.description());
        product.setSku(request.sku().trim());
        product.setPrice(request.price());
        product.setStockQuantity(request.stockQuantity());
    }
    
}
