package edu.miu.ads.dentalsurgery.dto;

import java.time.Instant;

public record PatientResponse(
        Long id,
        String firstName,
        String lastName,
        String phone,
        String email,
        Instant createdAt,
        Instant updatedAt) {
}
