package com.eldercare360.backend.dto;

import jakarta.validation.constraints.NotBlank;

public record MedicalRecordRequest(
        @NotBlank String title,
        @NotBlank String documentType,
        String summary
) {
}
