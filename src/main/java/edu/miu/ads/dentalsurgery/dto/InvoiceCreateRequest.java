package edu.miu.ads.dentalsurgery.dto;

import java.math.BigDecimal;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public record InvoiceCreateRequest(
        @NotNull(message = "appointmentId is required")
        Long appointmentId,

        @NotNull(message = "taxAmount is required")
        @DecimalMin(value = "0.0", inclusive = true, message = "taxAmount must be >= 0")
        BigDecimal taxAmount,

        @NotNull(message = "discountAmount is required")
        @DecimalMin(value = "0.0", inclusive = true, message = "discountAmount must be >= 0")
        BigDecimal discountAmount,

        @Size(max = 500, message = "notes must be at most 500 characters")
        String notes) {
}
