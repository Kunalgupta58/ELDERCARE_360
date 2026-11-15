package com.eldercare360.backend.controller;

import com.eldercare360.backend.dto.AppointmentRequest;
import com.eldercare360.backend.model.AppUser;
import com.eldercare360.backend.model.Appointment;
import com.eldercare360.backend.service.AppointmentService;
import com.eldercare360.backend.service.UserService;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api")
public class AppointmentController {
    private final AppointmentService appointmentService;
    private final UserService userService;

    public AppointmentController(AppointmentService appointmentService, UserService userService) {
        this.appointmentService = appointmentService;
        this.userService = userService;
    }

    @GetMapping("/appointments")
    public ResponseEntity<List<Appointment>> getAppointments() {
        AppUser user = userService.getCurrentUser();
        return ResponseEntity.ok(appointmentService.getAllForUser(user));
    }

    @PostMapping("/appointments")
    public ResponseEntity<Appointment> createAppointment(@Valid @RequestBody AppointmentRequest request) {
        AppUser user = userService.getCurrentUser();
        return ResponseEntity.ok(appointmentService.create(user, request));
    }
}
