package com.eldercare360.backend.dto;

public record UserSummary(
        Long id,
        String fullName,
        String email,
        String phone,
        String role
) {
}
