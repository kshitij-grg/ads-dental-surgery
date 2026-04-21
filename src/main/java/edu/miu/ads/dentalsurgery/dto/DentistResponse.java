package edu.miu.ads.dentalsurgery.dto;

import java.time.Instant;

public record DentistResponse(
        Long id,
        String firstName,
        String lastName,
        String specialty,
        String licenseNumber,
        Instant createdAt,
        Instant updatedAt) {
}
