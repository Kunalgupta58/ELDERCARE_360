package com.eldercare360.backend.dto;

public record DashboardSummary(
        long medicationCount,
        long appointmentCount,
        long recordCount,
        String nextMedication,
        String nextAppointment,
        double completionRate
) {
}
