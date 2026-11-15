package com.eldercare360.backend.dto;

import java.time.Instant;

public record MedicalRecordResponse(
        Long id,
        String title,
        String documentType,
        String fileName,
        String summary,
        String extractedText,
        Instant createdAt
) {
}
