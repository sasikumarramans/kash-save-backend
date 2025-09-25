package com.evbooking.backend.usecase.service;

import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

@Service
public class TempPdfStorageService {

    private final Map<String, TempPdfData> tempStorage = new ConcurrentHashMap<>();

    public TempPdfInfo storePdf(byte[] pdfBytes, String fileName, Long userId) {
        String token = generateToken();
        LocalDateTime expiresAt = LocalDateTime.now().plusHours(1); // 1 hour expiration

        TempPdfData pdfData = new TempPdfData(pdfBytes, fileName, userId, expiresAt);
        tempStorage.put(token, pdfData);

        // Clean up expired entries
        cleanupExpiredPdfs();

        return new TempPdfInfo(token, fileName, expiresAt);
    }

    public TempPdfData retrievePdf(String token, Long userId) {
        TempPdfData pdfData = tempStorage.get(token);

        if (pdfData == null) {
            return null; // Token not found
        }

        if (LocalDateTime.now().isAfter(pdfData.getExpiresAt())) {
            tempStorage.remove(token);
            return null; // Expired
        }

        if (!pdfData.getUserId().equals(userId)) {
            return null; // Not authorized
        }

        return pdfData;
    }

    public void removePdf(String token) {
        tempStorage.remove(token);
    }

    private String generateToken() {
        return UUID.randomUUID().toString();
    }

    @Scheduled(fixedRate = 300000) // Run every 5 minutes
    public void cleanupExpiredPdfs() {
        LocalDateTime now = LocalDateTime.now();
        tempStorage.entrySet().removeIf(entry -> now.isAfter(entry.getValue().getExpiresAt()));
    }

    public static class TempPdfInfo {
        private final String token;
        private final String fileName;
        private final LocalDateTime expiresAt;

        public TempPdfInfo(String token, String fileName, LocalDateTime expiresAt) {
            this.token = token;
            this.fileName = fileName;
            this.expiresAt = expiresAt;
        }

        public String getToken() { return token; }
        public String getFileName() { return fileName; }
        public LocalDateTime getExpiresAt() { return expiresAt; }
    }

    public static class TempPdfData {
        private final byte[] pdfBytes;
        private final String fileName;
        private final Long userId;
        private final LocalDateTime expiresAt;

        public TempPdfData(byte[] pdfBytes, String fileName, Long userId, LocalDateTime expiresAt) {
            this.pdfBytes = pdfBytes;
            this.fileName = fileName;
            this.userId = userId;
            this.expiresAt = expiresAt;
        }

        public byte[] getPdfBytes() { return pdfBytes; }
        public String getFileName() { return fileName; }
        public Long getUserId() { return userId; }
        public LocalDateTime getExpiresAt() { return expiresAt; }
    }
}