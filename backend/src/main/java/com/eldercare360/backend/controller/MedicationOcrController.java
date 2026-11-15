package com.eldercare360.backend.controller;

import com.eldercare360.backend.dto.OcrResponse;
import com.eldercare360.backend.service.MedicationOcrService;
import jakarta.validation.constraints.NotNull;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;

@RestController
@RequestMapping("/api/medications")
public class MedicationOcrController {
    private final MedicationOcrService medicationOcrService;

    public MedicationOcrController(MedicationOcrService medicationOcrService) {
        this.medicationOcrService = medicationOcrService;
    }

    @PostMapping("/ocr")
    public ResponseEntity<OcrResponse> readPrescription(@RequestParam @NotNull MultipartFile file) throws IOException {
        return ResponseEntity.ok(medicationOcrService.extract(file));
    }
}
