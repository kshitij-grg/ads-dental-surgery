package edu.miu.ads.dentalsurgery.dto;

import java.math.BigDecimal;
import java.time.Instant;

public record TreatmentResponse(
        Long id,
        String code,
        String name,
        String description,
        BigDecimal baseCost,
        Integer durationMinutes,
        boolean active,
        Instant createdAt,
        Instant updatedAt) {
}
