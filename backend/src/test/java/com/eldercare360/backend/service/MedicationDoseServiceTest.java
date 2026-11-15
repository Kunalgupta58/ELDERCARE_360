package com.eldercare360.backend.service;

import com.eldercare360.backend.model.AppUser;
import com.eldercare360.backend.model.Medication;
import com.eldercare360.backend.model.MedicationDose;
import com.eldercare360.backend.model.MedicationStatus;
import com.eldercare360.backend.repository.MedicationDoseRepository;
import com.eldercare360.backend.repository.MedicationRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.web.server.ResponseStatusException;

import java.time.LocalDate;
import java.time.ZoneId;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.*;

class MedicationDoseServiceTest {
    private MedicationRepository medicationRepository;
    private MedicationDoseRepository doseRepository;
    private MedicationDoseService service;

    @BeforeEach
    void setUp() {
        medicationRepository = mock(MedicationRepository.class);
        doseRepository = mock(MedicationDoseRepository.class);
        service = new MedicationDoseService(medicationRepository, doseRepository);
    }

    @Test
    void confirmsEachScheduledTimeIndependently() {
        AppUser owner = new AppUser();
        Medication medication = new Medication();
        medication.setId(9L);
        medication.setUser(owner);
        medication.setName("Medicine");
        medication.setDosage("1 tablet");
        medication.setScheduleTime("08:00,20:00");
        MedicationDose morning = dose(medication, "08:00");
        MedicationDose evening = dose(medication, "20:00");
        LocalDate today = LocalDate.now(ZoneId.of("UTC"));
        when(medicationRepository.findByIdAndUser(9L, owner)).thenReturn(Optional.of(medication));
        when(doseRepository.findByMedicationAndDoseDateAndScheduledTime(medication, today, "08:00"))
                .thenReturn(Optional.of(morning));
        when(doseRepository.findByMedicationAndDoseDateAndScheduledTime(medication, today, "20:00"))
                .thenReturn(Optional.of(evening));
        when(doseRepository.save(any(MedicationDose.class))).thenAnswer(invocation -> invocation.getArgument(0));

        service.updateStatus(9L, "08:00", owner, MedicationStatus.TAKEN, ZoneId.of("UTC"));

        assertEquals(MedicationStatus.TAKEN, morning.getStatus());
        assertEquals(MedicationStatus.PENDING, evening.getStatus());

        service.updateStatus(9L, "20:00", owner, MedicationStatus.TAKEN, ZoneId.of("UTC"));

        assertEquals(MedicationStatus.TAKEN, morning.getStatus());
        assertEquals(MedicationStatus.TAKEN, evening.getStatus());
    }

    @Test
    void doesNotAllowUpdatingAnotherUsersMedication() {
        AppUser owner = new AppUser();
        when(medicationRepository.findByIdAndUser(9L, owner)).thenReturn(Optional.empty());

        assertThrows(ResponseStatusException.class,
                () -> service.updateStatus(9L, "08:00", owner, MedicationStatus.TAKEN, ZoneId.of("UTC")));
        verifyNoInteractions(doseRepository);
    }

    private static MedicationDose dose(Medication medication, String time) {
        MedicationDose dose = new MedicationDose();
        dose.setMedication(medication);
        dose.setDoseDate(LocalDate.now(ZoneId.of("UTC")));
        dose.setScheduledTime(time);
        dose.setStatus(MedicationStatus.PENDING);
        return dose;
    }
}
