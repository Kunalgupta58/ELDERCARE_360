package com.eldercare360.backend.controller;

import com.eldercare360.backend.dto.MedicationRequest;
import com.eldercare360.backend.model.AppUser;
import com.eldercare360.backend.model.Medication;
import com.eldercare360.backend.model.MedicationStatus;
import com.eldercare360.backend.service.MedicationService;
import com.eldercare360.backend.service.UserService;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api")
public class MedicationController {
    private final MedicationService medicationService;
    private final UserService userService;

    public MedicationController(MedicationService medicationService, UserService userService) {
        this.medicationService = medicationService;
        this.userService = userService;
    }

    @GetMapping("/medications")
    public ResponseEntity<List<Medication>> getMyMedications() {
        AppUser user = userService.getCurrentUser();
        return ResponseEntity.ok(medicationService.getAllForUser(user));
    }

    @PostMapping("/medications")
    public ResponseEntity<Medication> createMedication(@Valid @RequestBody MedicationRequest request) {
        AppUser user = userService.getCurrentUser();
        return ResponseEntity.ok(medicationService.create(user, request));
    }

    @PatchMapping("/medications/{id}/status")
    public ResponseEntity<Medication> updateStatus(@PathVariable Long id, @RequestParam String status) {
        return ResponseEntity.ok(medicationService.updateStatus(id, MedicationStatus.valueOf(status.toUpperCase())));
    }
}
