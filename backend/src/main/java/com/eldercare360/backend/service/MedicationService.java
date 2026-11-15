package com.eldercare360.backend.service;

import com.eldercare360.backend.dto.MedicationRequest;
import com.eldercare360.backend.model.AppUser;
import com.eldercare360.backend.model.Medication;
import com.eldercare360.backend.model.MedicationStatus;
import com.eldercare360.backend.repository.MedicationRepository;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.List;
import java.util.Locale;

@Service
public class MedicationService {
    private static final DateTimeFormatter TWELVE_HOUR_TIME = DateTimeFormatter.ofPattern("h:mm a", Locale.US);
    private final MedicationRepository medicationRepository;

    public MedicationService(MedicationRepository medicationRepository) {
        this.medicationRepository = medicationRepository;
    }

    public List<Medication> getAllForUser(AppUser user) {
        List<Medication> medications = medicationRepository.findByUserOrderByCreatedAtDesc(user);
        LocalDate today = LocalDate.now();
        boolean resetStatuses = false;
        for (Medication medication : medications) {
            if (medication.getStatus() != MedicationStatus.PENDING
                    && medication.getStatusUpdatedAt().atZone(ZoneId.systemDefault()).toLocalDate().isBefore(today)) {
                medication.setStatus(MedicationStatus.PENDING);
                medication.setStatusUpdatedAt(Instant.now());
                resetStatuses = true;
            }
        }
        return resetStatuses ? medicationRepository.saveAll(medications) : medications;
    }

    public Medication create(AppUser user, MedicationRequest request) {
        Medication medication = new Medication();
        medication.setUser(user);
        medication.setName(request.name());
        medication.setDosage(request.dosage());
        medication.setScheduleTime(String.join(",", parseScheduleTimes(request.scheduleTime())));
        medication.setFrequency(request.frequency());
        medication.setNotes(request.notes());
        medication.setStatus(MedicationStatus.PENDING);
        medication.setStatusUpdatedAt(Instant.now());
        return medicationRepository.save(medication);
    }

    public static List<String> parseScheduleTimes(String scheduleTime) {
        if (scheduleTime == null || scheduleTime.isBlank()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "At least one dose time is required");
        }

        String[] parts = scheduleTime.split(",");
        if (parts.length > 5) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "A medication can have at most five daily times");
        }

        List<String> times = new java.util.ArrayList<>();
        for (String part : parts) {
            String value = part.trim();
            try {
                LocalTime time;
                try {
                    time = LocalTime.parse(value);
                } catch (DateTimeParseException exception) {
                    time = LocalTime.parse(value.toUpperCase(Locale.US), TWELVE_HOUR_TIME);
                }
                String normalized = time.format(DateTimeFormatter.ofPattern("HH:mm"));
                if (times.contains(normalized)) {
                    throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Dose times must be unique");
                }
                times.add(normalized);
            } catch (DateTimeParseException exception) {
                throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Dose times must use HH:mm or h:mm AM/PM format");
            }
        }
        return List.copyOf(times);
    }

    public Medication updateStatus(Long id, AppUser user, MedicationStatus status) {
        Medication medication = medicationRepository.findByIdAndUser(id, user)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Medication not found"));
        medication.setStatus(status);
        medication.setStatusUpdatedAt(Instant.now());
        return medicationRepository.save(medication);
    }
}
