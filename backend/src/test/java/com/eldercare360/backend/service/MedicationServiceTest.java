package com.eldercare360.backend.service;

import com.eldercare360.backend.model.AppUser;
import com.eldercare360.backend.model.Medication;
import com.eldercare360.backend.model.MedicationStatus;
import com.eldercare360.backend.repository.MedicationRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.web.server.ResponseStatusException;

import java.time.LocalDate;
import java.time.ZoneId;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.*;

class MedicationServiceTest {
    private MedicationRepository medicationRepository;
    private MedicationService medicationService;

    @BeforeEach
    void setUp() {
        medicationRepository = mock(MedicationRepository.class);
        medicationService = new MedicationService(medicationRepository);
    }

    @Test
    void updatesMedicationStatusOnlyForTheOwningUser() {
        AppUser owner = new AppUser();
        Medication medication = new Medication();
        medication.setUser(owner);
        medication.setStatus(MedicationStatus.PENDING);

        when(medicationRepository.findByIdAndUser(42L, owner)).thenReturn(Optional.of(medication));
        when(medicationRepository.save(medication)).thenReturn(medication);

        Medication updated = medicationService.updateStatus(42L, owner, MedicationStatus.TAKEN);

        assertEquals(MedicationStatus.TAKEN, updated.getStatus());
        verify(medicationRepository).findByIdAndUser(42L, owner);
    }

    @Test
    void doesNotUpdateMedicationOwnedByAnotherUser() {
        AppUser user = new AppUser();
        when(medicationRepository.findByIdAndUser(42L, user)).thenReturn(Optional.empty());

        assertThrows(ResponseStatusException.class,
                () -> medicationService.updateStatus(42L, user, MedicationStatus.TAKEN));
        verify(medicationRepository, never()).save(any());
    }

    @Test
    void resetsYesterdayStatusWhenLoadingDailyMedications() {
        AppUser owner = new AppUser();
        Medication medication = new Medication();
        medication.setStatus(MedicationStatus.TAKEN);
        medication.setStatusUpdatedAt(LocalDate.now().minusDays(1)
                .atStartOfDay(ZoneId.systemDefault()).toInstant());
        when(medicationRepository.findByUserOrderByCreatedAtDesc(owner)).thenReturn(List.of(medication));
        when(medicationRepository.saveAll(List.of(medication))).thenReturn(List.of(medication));

        List<Medication> medications = medicationService.getAllForUser(owner);

        assertEquals(MedicationStatus.PENDING, medications.get(0).getStatus());
        assertEquals(LocalDate.now(), medications.get(0).getStatusUpdatedAt()
                .atZone(ZoneId.systemDefault()).toLocalDate());
        verify(medicationRepository).saveAll(List.of(medication));
    }
}
