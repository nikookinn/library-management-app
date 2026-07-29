package com.nikookinn.librarymanagement.dto.response;

public record AuthResponse(
        String token,
        String tokenType,
        String email,
        String role) {
}
