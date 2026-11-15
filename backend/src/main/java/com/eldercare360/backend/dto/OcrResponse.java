package com.eldercare360.backend.dto;

import java.util.List;

public record OcrResponse(String text, List<OcrMedication> medications, String status) {
}
