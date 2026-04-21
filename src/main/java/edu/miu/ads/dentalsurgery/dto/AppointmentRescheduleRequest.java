package edu.miu.ads.dentalsurgery.dto;

import java.time.LocalDateTime;

import jakarta.validation.constraints.NotNull;

public record AppointmentRescheduleRequest(
        @NotNull(message = "startAt is required")
        LocalDateTime startAt) {
}
