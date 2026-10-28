package com.ecommerce.backend.service;

public class ProductNotFoundException extends RuntimeException {

    public ProductNotFoundException(String message) {
        super(message);
    }
    
}
