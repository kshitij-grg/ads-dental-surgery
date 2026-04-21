package edu.miu.ads.dentalsurgery.dto;

import java.time.LocalDateTime;

import jakarta.validation.constraints.NotNull;

public record AppointmentRequest(
        @NotNull(message = "patientId is required")
        Long patientId,
        @NotNull(message = "dentistId is required")
        Long dentistId,
        @NotNull(message = "treatmentId is required")
        Long treatmentId,
        @NotNull(message = "startAt is required")
        LocalDateTime startAt) {
}
