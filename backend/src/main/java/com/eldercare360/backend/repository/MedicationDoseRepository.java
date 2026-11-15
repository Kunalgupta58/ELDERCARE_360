package com.eldercare360.backend.repository;

import com.eldercare360.backend.model.AppUser;
import com.eldercare360.backend.model.Medication;
import com.eldercare360.backend.model.MedicationDose;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

public interface MedicationDoseRepository extends JpaRepository<MedicationDose, Long> {
    List<MedicationDose> findByMedication_UserAndDoseDateOrderByScheduledTimeAsc(AppUser user, LocalDate doseDate);

    Optional<MedicationDose> findByMedicationAndDoseDateAndScheduledTime(
            Medication medication,
            LocalDate doseDate,
            String scheduledTime
    );
}
