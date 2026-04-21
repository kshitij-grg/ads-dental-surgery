package edu.miu.ads.dentalsurgery.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record AppointmentCancelRequest(
        @NotBlank(message = "cancellationReason is required")
        @Size(max = 500, message = "cancellationReason must be at most 500 characters")
        String cancellationReason) {
}
