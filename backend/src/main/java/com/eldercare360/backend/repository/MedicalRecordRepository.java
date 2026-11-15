package com.eldercare360.backend.repository;

import com.eldercare360.backend.model.AppUser;
import com.eldercare360.backend.model.MedicalRecord;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface MedicalRecordRepository extends JpaRepository<MedicalRecord, Long> {
    List<MedicalRecord> findByUserOrderByCreatedAtDesc(AppUser user);
}
