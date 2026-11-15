package com.eldercare360.backend.service;

import com.eldercare360.backend.dto.DashboardSummary;
import com.eldercare360.backend.model.Appointment;
import com.eldercare360.backend.model.AppUser;
import com.eldercare360.backend.model.Medication;
import com.eldercare360.backend.model.MedicationStatus;
import com.eldercare360.backend.repository.AppointmentRepository;
import com.eldercare360.backend.repository.MedicalRecordRepository;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.Comparator;
import java.util.List;

@Service
public class DashboardService {
    private final MedicationService medicationService;
    private final AppointmentRepository appointmentRepository;
    private final MedicalRecordRepository medicalRecordRepository;

    public DashboardService(MedicationService medicationService,
                           AppointmentRepository appointmentRepository,
                           MedicalRecordRepository medicalRecordRepository) {
        this.medicationService = medicationService;
        this.appointmentRepository = appointmentRepository;
        this.medicalRecordRepository = medicalRecordRepository;
    }

    public DashboardSummary buildForUser(AppUser user) {
        List<Medication> medications = medicationService.getAllForUser(user);
        long medicationCount = medications.size();
        long appointmentCount = appointmentRepository.findByUserOrderByAppointmentTimeAsc(user).size();
        long recordCount = medicalRecordRepository.findByUserOrderByCreatedAtDesc(user).size();

        Medication nextMedication = medications.stream()
                .filter(m -> m.getStatus() != MedicationStatus.TAKEN)
                .min(Comparator.comparing(m -> Instant.now()))
                .orElse(null);

        Appointment nextAppointment = appointmentRepository.findByUserOrderByAppointmentTimeAsc(user).stream()
                .filter(a -> a.getAppointmentTime().isAfter(Instant.now()))
                .findFirst()
                .orElse(null);

        double completionRate = medicationCount == 0 ? 0 : ((double) medications.stream()
                .filter(m -> m.getStatus() == MedicationStatus.TAKEN)
                .count() / (double) medicationCount) * 100.0;

        return new DashboardSummary(
                medicationCount,
                appointmentCount,
                recordCount,
                nextMedication == null ? "No upcoming medications" : nextMedication.getName() + " at " + nextMedication.getScheduleTime(),
                nextAppointment == null ? "No upcoming appointments" : nextAppointment.getTitle() + " on " + nextAppointment.getAppointmentTime(),
                Math.round(completionRate * 100.0) / 100.0
        );
    }
}
