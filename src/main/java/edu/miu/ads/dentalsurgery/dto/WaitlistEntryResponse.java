package edu.miu.ads.dentalsurgery.dto;

import java.time.Instant;
import java.time.LocalDateTime;

public record WaitlistEntryResponse(
        Long id,
        Long patientId,
        String patientName,
        Long treatmentId,
        String treatmentCode,
        String treatmentName,
        LocalDateTime preferredStartAt,
        LocalDateTime preferredEndAt,
        Integer priority,
        String notes,
        boolean active,
        Instant createdAt,
        Instant updatedAt) {
}