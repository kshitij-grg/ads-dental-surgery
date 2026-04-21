package edu.miu.ads.dentalsurgery.dto;

public record AuthResponse(
        String token,
        String tokenType,
        String username,
        String role,
        long expiresInMs) {
}
