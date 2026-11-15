package com.eldercare360.backend.controller;

import com.eldercare360.backend.model.AppUser;
import com.eldercare360.backend.model.MedicalRecord;
import com.eldercare360.backend.dto.MedicalRecordResponse;
import com.eldercare360.backend.service.MedicalRecordService;
import com.eldercare360.backend.service.UserService;
import org.springframework.core.io.FileSystemResource;
import org.springframework.core.io.Resource;
import org.springframework.http.ContentDisposition;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
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
    public ResponseEntity<List<MedicalRecordResponse>> getRecords() {
        AppUser user = userService.getCurrentUser();
        return ResponseEntity.ok(medicalRecordService.getAllForUser(user).stream()
                .map(MedicalRecordService::toResponse)
                .toList());
    }

    @PostMapping("/records")
    public ResponseEntity<MedicalRecordResponse> addRecord(
            @RequestParam String title,
            @RequestParam String documentType,
            @RequestParam(required = false) String summary,
            @RequestParam MultipartFile file) throws IOException {
        AppUser user = userService.getCurrentUser();
        MedicalRecord record = medicalRecordService.saveRecord(user, title, documentType, summary, file);
        return ResponseEntity.ok(MedicalRecordService.toResponse(record));
    }

    @GetMapping("/records/{id}/file")
    public ResponseEntity<Resource> downloadRecord(@PathVariable Long id) {
        AppUser user = userService.getCurrentUser();
        MedicalRecord record = medicalRecordService.getRecordForUser(user, id);
        Resource file = new FileSystemResource(medicalRecordService.getFileForUser(user, id));
        return ResponseEntity.ok()
                .contentType(MediaType.APPLICATION_OCTET_STREAM)
                .header(HttpHeaders.CONTENT_DISPOSITION, ContentDisposition.attachment()
                        .filename(record.getFileName(), StandardCharsets.UTF_8)
                        .build()
                        .toString())
                .body(file);
    }
}
