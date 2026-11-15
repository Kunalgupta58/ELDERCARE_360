package com.eldercare360.backend.controller;

import com.eldercare360.backend.model.AppUser;
import com.eldercare360.backend.model.MedicalRecord;
import com.eldercare360.backend.service.MedicalRecordService;
import com.eldercare360.backend.service.UserService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.List;

@RestController
@RequestMapping("/api")
public class MedicalRecordController {
    private final MedicalRecordService medicalRecordService;
    private final UserService userService;

    public MedicalRecordController(MedicalRecordService medicalRecordService, UserService userService) {
        this.medicalRecordService = medicalRecordService;
        this.userService = userService;
    }

    @GetMapping("/records")
    public ResponseEntity<List<MedicalRecord>> getRecords() {
        AppUser user = userService.getCurrentUser();
        return ResponseEntity.ok(medicalRecordService.getAllForUser(user));
    }

    @PostMapping("/records")
    public ResponseEntity<MedicalRecord> addRecord(
            @RequestParam String title,
            @RequestParam String documentType,
            @RequestParam(required = false) String summary,
            @RequestParam(required = false) MultipartFile file,
            @RequestParam(required = false) String extractedText) throws IOException {
        AppUser user = userService.getCurrentUser();
        return ResponseEntity.ok(medicalRecordService.saveRecord(user, title, documentType, summary, file, extractedText));
    }
}
