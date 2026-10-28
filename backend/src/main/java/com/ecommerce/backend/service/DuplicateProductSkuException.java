package com.ecommerce.backend.service;

public class DuplicateProductSkuException extends RuntimeException {

    public DuplicateProductSkuException(String message) {
        super(message);
    }
    
}
