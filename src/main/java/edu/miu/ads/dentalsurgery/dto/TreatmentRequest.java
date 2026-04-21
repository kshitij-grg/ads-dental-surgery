package edu.miu.ads.dentalsurgery.dto;

import java.math.BigDecimal;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public record TreatmentRequest(
        @NotBlank(message = "code is required")
        @Size(max = 50, message = "code must be at most 50 characters")
        String code,

        @NotBlank(message = "name is required")
        @Size(max = 120, message = "name must be at most 120 characters")
        String name,

        @Size(max = 500, message = "description must be at most 500 characters")
        String description,

        @NotNull(message = "baseCost is required")
        @DecimalMin(value = "0.0", inclusive = true, message = "baseCost must be >= 0")
        BigDecimal baseCost,

        @NotNull(message = "durationMinutes is required")
        @Min(value = 1, message = "durationMinutes must be >= 1")
        Integer durationMinutes,

        @NotNull(message = "active is required")
        Boolean active) {
}
