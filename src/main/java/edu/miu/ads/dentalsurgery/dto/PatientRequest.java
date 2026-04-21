package edu.miu.ads.dentalsurgery.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record PatientRequest(
        @NotBlank(message = "firstName is required")
        @Size(max = 80, message = "firstName must be at most 80 characters")
        String firstName,

        @NotBlank(message = "lastName is required")
        @Size(max = 80, message = "lastName must be at most 80 characters")
        String lastName,

        @Size(max = 30, message = "phone must be at most 30 characters")
        String phone,

        @Email(message = "email must be valid")
        @Size(max = 120, message = "email must be at most 120 characters")
        String email) {
}
