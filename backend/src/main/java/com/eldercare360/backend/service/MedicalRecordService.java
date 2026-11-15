package com.eldercare360.backend.service;

import com.eldercare360.backend.model.AppUser;
import com.eldercare360.backend.model.MedicalRecord;
import com.eldercare360.backend.repository.MedicalRecordRepository;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import java.util.UUID;

@Service
public class MedicalRecordService {
    private final MedicalRecordRepository medicalRecordRepository;

    public MedicalRecordService(MedicalRecordRepository medicalRecordRepository) {
        this.medicalRecordRepository = medicalRecordRepository;
    }

    public List<MedicalRecord> getAllForUser(AppUser user) {
        return medicalRecordRepository.findByUserOrderByCreatedAtDesc(user);
    }

    public MedicalRecord saveRecord(AppUser user, String title, String documentType, String summary, MultipartFile file, String extractedText) throws IOException {
        Path storageDirectory = Paths.get(System.getProperty("user.home"), "eldercare-files");
        Files.createDirectories(storageDirectory);

        String originalFileName = file == null || file.isEmpty() ? "document.txt" : file.getOriginalFilename();
        String safeFileName = UUID.randomUUID() + "_" + originalFileName;
        Path target = storageDirectory.resolve(safeFileName);
        if (file != null && !file.isEmpty()) {
            file.transferTo(target);
        }

        MedicalRecord record = new MedicalRecord();
        record.setUser(user);
        record.setTitle(title);
        record.setDocumentType(documentType);
        record.setSummary(summary);
        record.setFileName(originalFileName);
        record.setFilePath(target.toString());
        record.setExtractedText(extractedText == null || extractedText.isBlank() ? "OCR pending review." : extractedText);
        return medicalRecordRepository.save(record);
    }
}
