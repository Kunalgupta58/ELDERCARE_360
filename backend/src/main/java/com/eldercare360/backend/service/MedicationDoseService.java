package com.eldercare360.backend.service;

import com.eldercare360.backend.dto.MedicationDoseResponse;
import com.eldercare360.backend.model.AppUser;
import com.eldercare360.backend.model.Medication;
import com.eldercare360.backend.model.MedicationDose;
import com.eldercare360.backend.model.MedicationStatus;
import com.eldercare360.backend.repository.MedicationDoseRepository;
import com.eldercare360.backend.repository.MedicationRepository;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.List;

@Service
public class MedicationDoseService {
    private final MedicationRepository medicationRepository;
    private final MedicationDoseRepository doseRepository;

    public MedicationDoseService(
            MedicationRepository medicationRepository,
            MedicationDoseRepository doseRepository
    ) {
        this.medicationRepository = medicationRepository;
        this.doseRepository = doseRepository;
    }

    @Transactional
    public List<MedicationDoseResponse> getTodayForUser(AppUser user, ZoneId zoneId) {
        LocalDate today = LocalDate.now(zoneId);
        List<Medication> medications = medicationRepository.findByUserOrderByCreatedAtDesc(user);
        for (Medication medication : medications) {
            if (!isDaily(medication.getFrequency())) continue;
            for (String time : MedicationService.parseScheduleTimes(medication.getScheduleTime())) {
                doseRepository.findByMedicationAndDoseDateAndScheduledTime(medication, today, time)
                        .orElseGet(() -> createPendingDose(medication, today, time));
            }
        }
        return doseRepository.findByMedication_UserAndDoseDateOrderByScheduledTimeAsc(user, today)
                .stream()
                .map(MedicationDoseService::toResponse)
                .toList();
    }

    @Transactional
    public MedicationDoseResponse updateStatus(
            Long medicationId,
            String scheduledTime,
            AppUser user,
            MedicationStatus status,
            ZoneId zoneId
    ) {
        Medication medication = medicationRepository.findByIdAndUser(medicationId, user)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Medication not found"));
        List<String> requestedTimes = MedicationService.parseScheduleTimes(scheduledTime);
        if (requestedTimes.size() != 1) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Update one dose time at a time");
        }
        String normalizedTime = requestedTimes.get(0);
        if (!MedicationService.parseScheduleTimes(medication.getScheduleTime()).contains(normalizedTime)) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Medication dose not found");
        }

        LocalDate today = LocalDate.now(zoneId);
        MedicationDose dose = doseRepository
                .findByMedicationAndDoseDateAndScheduledTime(medication, today, normalizedTime)
                .orElseGet(() -> createPendingDose(medication, today, normalizedTime));
        dose.setStatus(status);
        dose.setStatusUpdatedAt(Instant.now());
        return toResponse(doseRepository.save(dose));
    }

    private MedicationDose createPendingDose(Medication medication, LocalDate date, String time) {
        MedicationDose dose = new MedicationDose();
        dose.setMedication(medication);
        dose.setDoseDate(date);
        dose.setScheduledTime(time);
        return doseRepository.save(dose);
    }

    private static boolean isDaily(String frequency) {
        return frequency != null && frequency.matches("(?i).*\\b(daily|every day)\\b.*");
    }

    private static MedicationDoseResponse toResponse(MedicationDose dose) {
        Medication medication = dose.getMedication();
        return new MedicationDoseResponse(
                dose.getId(),
                medication.getId(),
                medication.getName(),
                medication.getDosage(),
                dose.getDoseDate(),
                dose.getScheduledTime(),
                dose.getStatus(),
                dose.getStatusUpdatedAt()
        );
    }
}
