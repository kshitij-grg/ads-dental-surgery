package edu.miu.ads.dentalsurgery.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record InvoiceMarkPaidRequest(
        @NotBlank(message = "paymentReference is required")
        @Size(max = 120, message = "paymentReference must be at most 120 characters")
        String paymentReference) {
}
