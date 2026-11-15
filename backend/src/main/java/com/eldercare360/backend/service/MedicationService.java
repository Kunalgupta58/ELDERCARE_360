package com.eldercare360.backend.service;

import com.eldercare360.backend.dto.MedicationRequest;
import com.eldercare360.backend.model.AppUser;
import com.eldercare360.backend.model.Medication;
import com.eldercare360.backend.model.MedicationStatus;
import com.eldercare360.backend.repository.MedicationRepository;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class MedicationService {
    private final MedicationRepository medicationRepository;

    public MedicationService(MedicationRepository medicationRepository) {
        this.medicationRepository = medicationRepository;
    }

    public List<Medication> getAllForUser(AppUser user) {
        return medicationRepository.findByUserOrderByCreatedAtDesc(user);
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
        return medicationRepository.save(medication);
    }

    public Medication updateStatus(Long id, MedicationStatus status) {
        Medication medication = medicationRepository.findById(id).orElseThrow(() -> new IllegalArgumentException("Medication not found"));
        medication.setStatus(status);
        return medicationRepository.save(medication);
    }
}
