package com.eldercare360.backend.repository;

import com.eldercare360.backend.model.AppUser;
import com.eldercare360.backend.model.Medication;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface MedicationRepository extends JpaRepository<Medication, Long> {
    List<Medication> findByUserOrderByCreatedAtDesc(AppUser user);
    Optional<Medication> findByIdAndUser(Long id, AppUser user);
    long countByUser(AppUser user);
}
