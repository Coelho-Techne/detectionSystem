package com.coelhotechne.detection_system.security.api.dto;

public record LoginResponse(
        String token,
        String tokenType,
        String email,
        String role
) {
    public static LoginResponse bearer(String token, String email, String role) {
        //portador
        return new LoginResponse(token, "Bearer", email, role);
    }
}
