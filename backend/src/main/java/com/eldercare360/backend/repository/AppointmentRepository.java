package com.eldercare360.backend.repository;

import com.eldercare360.backend.model.Appointment;
import com.eldercare360.backend.model.AppUser;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface AppointmentRepository extends JpaRepository<Appointment, Long> {
    List<Appointment> findByUserOrderByAppointmentTimeAsc(AppUser user);
}
