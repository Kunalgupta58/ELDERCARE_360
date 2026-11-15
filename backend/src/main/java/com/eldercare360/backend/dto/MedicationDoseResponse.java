package com.eldercare360.backend.dto;

import com.eldercare360.backend.model.MedicationStatus;

import java.time.Instant;
import java.time.LocalDate;

public record MedicationDoseResponse(
        Long id,
        Long medicationId,
        String medicationName,
        String dosage,
        LocalDate doseDate,
        String scheduledTime,
        MedicationStatus status,
        Instant statusUpdatedAt
) {
}
