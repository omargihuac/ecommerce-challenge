
package com.ecommerce.backend.dto;

import java.math.BigDecimal;
import java.time.OffsetDateTime;

import jakarta.validation.constraints.DecimalMax;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public record PromotionRequest(
    @NotBlank(message = "El nombre de la promoción es obligatorio")
    @Size(max = 30, message = "El nombre no puede superar 30 caracteres")
    String name,

    String description,

    @NotNull(message = "El porcentaje de descuento es obligatorio")
    @DecimalMin(
        value = "0.0",
        inclusive = false,
        message = "El descuento debe ser mayor que cero"
    )
    @DecimalMax(
        value = "100.0",
        message = "El descuento no puede superar el 100%"
    )
    BigDecimal discountPercentage,

    @NotNull(message = "La fecha de inicio es obligatoria")
    OffsetDateTime startsAt,

    @NotNull(message = "La fecha de finalización es obligatoria")
    OffsetDateTime endsAt
) {}
