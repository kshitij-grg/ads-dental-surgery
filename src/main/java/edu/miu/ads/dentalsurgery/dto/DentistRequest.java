package edu.miu.ads.dentalsurgery.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record DentistRequest(
        @NotBlank(message = "firstName is required")
        @Size(max = 80, message = "firstName must be at most 80 characters")
        String firstName,

        @NotBlank(message = "lastName is required")
        @Size(max = 80, message = "lastName must be at most 80 characters")
        String lastName,

        @NotBlank(message = "specialty is required")
        @Size(max = 120, message = "specialty must be at most 120 characters")
        String specialty,

        @NotBlank(message = "licenseNumber is required")
        @Size(max = 80, message = "licenseNumber must be at most 80 characters")
        String licenseNumber) {
}
