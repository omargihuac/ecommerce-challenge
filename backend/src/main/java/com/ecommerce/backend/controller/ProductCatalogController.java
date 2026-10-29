
package com.ecommerce.backend.controller;

import java.util.List;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.ecommerce.backend.dto.ProductCatalogResponse;
import com.ecommerce.backend.service.ProductCatalogService;

@RestController
@RequestMapping("/api/catalog/products")
public class ProductCatalogController {

    private final ProductCatalogService productCatalogService;

    public ProductCatalogController(
            ProductCatalogService productCatalogService) {
        this.productCatalogService = productCatalogService;
    }

    @GetMapping
    public List<ProductCatalogResponse> findAll() {
        return productCatalogService.findAll();
    }
}
