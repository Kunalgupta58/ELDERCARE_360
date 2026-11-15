package com.eldercare360.backend.service;

import com.eldercare360.backend.dto.AppointmentRequest;
import com.eldercare360.backend.model.Appointment;
import com.eldercare360.backend.model.AppUser;
import com.eldercare360.backend.model.AppointmentStatus;
import com.eldercare360.backend.repository.AppointmentRepository;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class AppointmentService {
    private final AppointmentRepository appointmentRepository;

    public AppointmentService(AppointmentRepository appointmentRepository) {
        this.appointmentRepository = appointmentRepository;
    }

    public List<Appointment> getAllForUser(AppUser user) {
        return appointmentRepository.findByUserOrderByAppointmentTimeAsc(user);
    }

    public Appointment create(AppUser user, AppointmentRequest request) {
        Appointment appointment = new Appointment();
        appointment.setUser(user);
        appointment.setTitle(request.title());
        appointment.setAppointmentTime(request.appointmentTime());
        appointment.setDoctorName(request.doctorName());
        appointment.setLocation(request.location());
        appointment.setNotes(request.notes());
        appointment.setStatus(AppointmentStatus.SCHEDULED);
        return appointmentRepository.save(appointment);
    }
}
