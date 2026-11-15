package com.eldercare360.backend.dto;

import jakarta.validation.constraints.NotBlank;

import java.time.Instant;

public record AppointmentRequest(
        @NotBlank String title,
        @NotBlank Instant appointmentTime,
        String doctorName,
        String location,
        String notes
) {
}
