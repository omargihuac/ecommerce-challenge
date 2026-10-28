package com.ecommerce.backend.dto;

import java.math.BigDecimal;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PositiveOrZero;
import jakarta.validation.constraints.Size;

public record ProductRequest (
    @NotBlank(message = "El nombre es obligatorio")
    @Size(max = 150, message = "El nombre no puede superar 30 caracteres")
    String name,

    String description,

    @NotBlank(message = "El SKU es obligatorio")
    @Size(max = 80, message = "El SKU no puede superar 10 caracteres")
    String sku,

    @NotNull(message = "El precio es obligatorio")
    @DecimalMin(value = "0.0", inclusive = false,
        message = "El precio debe ser mayor que cero")
    BigDecimal price,

    @NotNull(message = "El stock es obligatorio")
    @PositiveOrZero(message = "El stock no puede ser negativo")
    Integer stockQuantity
){}
