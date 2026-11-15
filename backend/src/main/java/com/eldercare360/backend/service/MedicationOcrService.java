package com.eldercare360.backend.service;

import com.eldercare360.backend.dto.OcrResponse;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.HttpStatusCodeException;
import org.springframework.web.client.ResourceAccessException;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.server.ResponseStatusException;

import java.io.IOException;
import java.util.Locale;
import java.util.Set;

import static org.springframework.http.HttpStatus.BAD_GATEWAY;
import static org.springframework.http.HttpStatus.BAD_REQUEST;
import static org.springframework.http.HttpStatus.SERVICE_UNAVAILABLE;

@Service
public class MedicationOcrService {
    private static final Set<String> ALLOWED_EXTENSIONS = Set.of(".pdf", ".png", ".jpg", ".jpeg", ".webp");
    private final RestTemplate restTemplate = new RestTemplate();
    private final String serviceUrl;

    public MedicationOcrService(@Value("${ocr.service.url}") String serviceUrl) {
        this.serviceUrl = serviceUrl;
    }

    public OcrResponse extract(MultipartFile file) throws IOException {
        if (file == null || file.isEmpty()) {
            throw new ResponseStatusException(BAD_REQUEST, "Choose a prescription or medicine-label image/PDF");
        }
        String fileName = file.getOriginalFilename();
        String safeName = fileName == null ? "" : fileName.replace('\\', '/');
        int separator = safeName.lastIndexOf('/');
        safeName = safeName.substring(separator + 1);
        int dot = safeName.lastIndexOf('.');
        String extension = dot < 0 ? "" : safeName.substring(dot).toLowerCase(Locale.ROOT);
        if (!ALLOWED_EXTENSIONS.contains(extension)) {
            throw new ResponseStatusException(BAD_REQUEST, "Use a PDF, PNG, JPG, or WEBP file");
        }
        String uploadName = safeName;

        ByteArrayResource fileResource = new ByteArrayResource(file.getBytes()) {
            @Override
            public String getFilename() {
                return uploadName;
            }
        };
        HttpHeaders partHeaders = new HttpHeaders();
        partHeaders.setContentType(MediaType.APPLICATION_OCTET_STREAM);
        MultiValueMap<String, Object> parts = new LinkedMultiValueMap<>();
        parts.add("file", new HttpEntity<>(fileResource, partHeaders));
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.MULTIPART_FORM_DATA);

        try {
            OcrResponse response = restTemplate.postForObject(
                    serviceUrl + "/extract",
                    new HttpEntity<>(parts, headers),
                    OcrResponse.class
            );
            if (response == null) {
                throw new ResponseStatusException(BAD_GATEWAY, "The OCR service returned an empty response");
            }
            return response;
        } catch (HttpStatusCodeException exception) {
            if (exception.getStatusCode().value() == 503) {
                throw new ResponseStatusException(
                        SERVICE_UNAVAILABLE,
                        "Local image OCR requires Tesseract to be installed in the running OCR service",
                        exception
                );
            }
            throw new ResponseStatusException(BAD_GATEWAY, "The OCR service could not read this file", exception);
        } catch (ResourceAccessException exception) {
            throw new ResponseStatusException(SERVICE_UNAVAILABLE, "The local OCR service is not running", exception);
        } catch (RestClientException exception) {
            throw new ResponseStatusException(BAD_GATEWAY, "The local OCR service returned an invalid response", exception);
        }
    }
}
