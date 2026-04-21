package edu.miu.ads.dentalsurgery.dto;

import java.time.Instant;
import java.time.LocalDateTime;

public record AppointmentResponse(
        Long id,
        Long patientId,
        String patientName,
        Long dentistId,
        String dentistName,
        Long treatmentId,
        String treatmentCode,
        String treatmentName,
        LocalDateTime startAt,
        LocalDateTime endAt,
        String status,
        String cancellationReason,
        Instant createdAt,
        Instant updatedAt) {
}
