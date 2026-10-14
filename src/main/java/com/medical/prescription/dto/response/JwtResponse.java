package com.medical.prescription.dto.response;

public record JwtResponse(String token, String tokenType, long expiresIn, String username, String role) {

    public static JwtResponse of(String token, long expiresIn, String username, String role) {
        return new JwtResponse(token, "Bearer", expiresIn, username, role);
    }
}
