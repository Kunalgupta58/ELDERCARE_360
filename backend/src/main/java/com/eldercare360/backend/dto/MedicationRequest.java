package com.eldercare360.backend.dto;

import jakarta.validation.constraints.NotBlank;

public record MedicationRequest(
        @NotBlank String name,
        @NotBlank String dosage,
        @NotBlank String scheduleTime,
        @NotBlank String frequency,
        String notes
) {
}
