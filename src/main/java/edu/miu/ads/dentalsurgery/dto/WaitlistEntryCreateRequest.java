package edu.miu.ads.dentalsurgery.dto;

import java.time.LocalDateTime;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public record WaitlistEntryCreateRequest(
        @NotNull(message = "patientId is required")
        Long patientId,
        @NotNull(message = "treatmentId is required")
        Long treatmentId,
        LocalDateTime preferredStartAt,
        LocalDateTime preferredEndAt,
        @NotNull(message = "priority is required")
        @Min(value = 1, message = "priority must be between 1 and 10")
        @Max(value = 10, message = "priority must be between 1 and 10")
        Integer priority,
        @Size(max = 500, message = "notes must be <= 500 characters")
        String notes) {
}