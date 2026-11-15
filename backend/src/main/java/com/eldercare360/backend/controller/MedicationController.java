package com.eldercare360.backend.controller;

import com.eldercare360.backend.dto.MedicationRequest;
import com.eldercare360.backend.dto.MedicationDoseResponse;
import com.eldercare360.backend.model.AppUser;
import com.eldercare360.backend.model.Medication;
import com.eldercare360.backend.model.MedicationStatus;
import com.eldercare360.backend.service.MedicationDoseService;
import com.eldercare360.backend.service.MedicationService;
import com.eldercare360.backend.service.UserService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

import java.time.DateTimeException;
import java.time.ZoneId;
import java.util.List;

@RestController
@RequestMapping("/api")
public class MedicationController {
    private final MedicationService medicationService;
    private final MedicationDoseService medicationDoseService;
    private final UserService userService;

    public MedicationController(
            MedicationService medicationService,
            MedicationDoseService medicationDoseService,
            UserService userService
    ) {
        this.medicationService = medicationService;
        this.medicationDoseService = medicationDoseService;
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

    @GetMapping("/medications/doses/today")
    public ResponseEntity<List<MedicationDoseResponse>> getTodayDoses(
            @RequestHeader(value = "X-Timezone", defaultValue = "UTC") String timezone
    ) {
        AppUser user = userService.getCurrentUser();
        return ResponseEntity.ok(medicationDoseService.getTodayForUser(user, parseZoneId(timezone)));
    }

    @PatchMapping("/medications/{id}/doses/status")
    public ResponseEntity<MedicationDoseResponse> updateDoseStatus(
            @PathVariable Long id,
            @RequestParam String scheduledTime,
            @RequestParam String status,
            @RequestHeader(value = "X-Timezone", defaultValue = "UTC") String timezone
    ) {
        AppUser user = userService.getCurrentUser();
        MedicationStatus parsedStatus;
        try {
            parsedStatus = MedicationStatus.valueOf(status.toUpperCase());
        } catch (IllegalArgumentException exception) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Invalid medication status");
        }
        return ResponseEntity.ok(medicationDoseService.updateStatus(
                id,
                scheduledTime,
                user,
                parsedStatus,
                parseZoneId(timezone)
        ));
    }

    private static ZoneId parseZoneId(String timezone) {
        try {
            return ZoneId.of(timezone);
        } catch (DateTimeException exception) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Invalid timezone", exception);
        }
    }

    @PatchMapping("/medications/{id}/status")
    public ResponseEntity<Medication> updateStatus(@PathVariable Long id, @RequestParam String status) {
        AppUser user = userService.getCurrentUser();
        return ResponseEntity.ok(medicationService.updateStatus(id, user, MedicationStatus.valueOf(status.toUpperCase())));
    }
}
