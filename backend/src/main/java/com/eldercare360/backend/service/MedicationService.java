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
import java.time.ZoneId;
import java.util.List;

@Service
public class MedicationService {
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
        medication.setScheduleTime(request.scheduleTime());
        medication.setFrequency(request.frequency());
        medication.setNotes(request.notes());
        medication.setStatus(MedicationStatus.PENDING);
        medication.setStatusUpdatedAt(Instant.now());
        return medicationRepository.save(medication);
    }

    public Medication updateStatus(Long id, AppUser user, MedicationStatus status) {
        Medication medication = medicationRepository.findByIdAndUser(id, user)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Medication not found"));
        medication.setStatus(status);
        medication.setStatusUpdatedAt(Instant.now());
        return medicationRepository.save(medication);
    }
}
