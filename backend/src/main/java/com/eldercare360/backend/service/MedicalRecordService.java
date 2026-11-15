package com.eldercare360.backend.service;

import com.eldercare360.backend.dto.MedicalRecordResponse;
import com.eldercare360.backend.model.AppUser;
import com.eldercare360.backend.model.MedicalRecord;
import com.eldercare360.backend.repository.MedicalRecordRepository;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.server.ResponseStatusException;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import java.util.Locale;
import java.util.UUID;

@Service
public class MedicalRecordService {
    private final MedicalRecordRepository medicalRecordRepository;
    private final Path storageDirectory;

    public MedicalRecordService(
            MedicalRecordRepository medicalRecordRepository,
            @Value("${storage.location}") String storageLocation
    ) {
        this.medicalRecordRepository = medicalRecordRepository;
        this.storageDirectory = Paths.get(storageLocation).toAbsolutePath().normalize();
    }

    public List<MedicalRecord> getAllForUser(AppUser user) {
        return medicalRecordRepository.findByUserOrderByCreatedAtDesc(user);
    }

    public MedicalRecord getRecordForUser(AppUser user, Long recordId) {
        return medicalRecordRepository.findByIdAndUser(recordId, user)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Record not found"));
    }

    public MedicalRecord saveRecord(
            AppUser user,
            String title,
            String documentType,
            String summary,
            MultipartFile file
    ) throws IOException {
        if (file == null || file.isEmpty()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Choose a file to store");
        }
        if (title == null || title.isBlank()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Record title is required");
        }

        String originalFileName = file.getOriginalFilename();
        if (originalFileName == null || originalFileName.isBlank()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "The uploaded file needs a name");
        }
        String safeOriginalName = Path.of(originalFileName.replace('\\', '/')).getFileName().toString();
        String extension = getAllowedExtension(safeOriginalName);
        Path target = storageDirectory.resolve(UUID.randomUUID() + extension).normalize();
        if (!target.startsWith(storageDirectory)) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Invalid file name");
        }

        Files.createDirectories(storageDirectory);
        file.transferTo(target);

        MedicalRecord record = new MedicalRecord();
        record.setUser(user);
        record.setTitle(title);
        record.setDocumentType(documentType);
        record.setSummary(summary);
        record.setFileName(safeOriginalName);
        record.setFilePath(target.toString());
        record.setExtractedText(null);
        return medicalRecordRepository.save(record);
    }

    public Path getFileForUser(AppUser user, Long recordId) {
        MedicalRecord record = getRecordForUser(user, recordId);
        Path filePath = Paths.get(record.getFilePath()).toAbsolutePath().normalize();
        if (!filePath.startsWith(storageDirectory) || !Files.isRegularFile(filePath)) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Record file not found");
        }
        return filePath;
    }

    public static MedicalRecordResponse toResponse(MedicalRecord record) {
        return new MedicalRecordResponse(
                record.getId(),
                record.getTitle(),
                record.getDocumentType(),
                record.getFileName(),
                record.getSummary(),
                record.getExtractedText(),
                record.getCreatedAt()
        );
    }

    private static String getAllowedExtension(String fileName) {
        int dot = fileName.lastIndexOf('.');
        String extension = dot < 0 ? "" : fileName.substring(dot).toLowerCase(Locale.ROOT);
        if (!List.of(".pdf", ".png", ".jpg", ".jpeg", ".webp", ".txt").contains(extension)) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "Supported files are PDF, PNG, JPG, WEBP, and TXT"
            );
        }
        return extension;
    }
}
