package com.evbooking.backend.presentation.dto;

import com.fasterxml.jackson.annotation.JsonFormat;

import java.time.LocalDateTime;

public class PdfDownloadResponse {
    private String downloadUrl;
    private String fileName;

    @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss")
    private LocalDateTime expiresAt;

    public PdfDownloadResponse() {}

    public PdfDownloadResponse(String downloadUrl, String fileName, LocalDateTime expiresAt) {
        this.downloadUrl = downloadUrl;
        this.fileName = fileName;
        this.expiresAt = expiresAt;
    }

    public String getDownloadUrl() { return downloadUrl; }
    public void setDownloadUrl(String downloadUrl) { this.downloadUrl = downloadUrl; }

    public String getFileName() { return fileName; }
    public void setFileName(String fileName) { this.fileName = fileName; }

    public LocalDateTime getExpiresAt() { return expiresAt; }
    public void setExpiresAt(LocalDateTime expiresAt) { this.expiresAt = expiresAt; }
}