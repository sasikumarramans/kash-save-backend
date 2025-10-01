package com.evbooking.backend.presentation.controller.split;

import com.evbooking.backend.presentation.dto.ApiResponse;
import com.evbooking.backend.usecase.service.split.PdfExportService;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

@RestController
@RequestMapping("/splits/export")
public class PdfExportController {

    private final PdfExportService pdfExportService;
    private static final DateTimeFormatter FILENAME_FORMATTER = DateTimeFormatter.ofPattern("yyyyMMdd_HHmmss");

    public PdfExportController(PdfExportService pdfExportService) {
        this.pdfExportService = pdfExportService;
    }

    /**
     * Export group expense report as PDF
     */
    @GetMapping("/groups/{groupId}/pdf")
    public ResponseEntity<?> exportGroupReport(@PathVariable Long groupId,
                                             @RequestParam(defaultValue = "false") boolean download,
                                             HttpServletRequest httpRequest) {
        try {
            Long userId = (Long) httpRequest.getAttribute("userId");
            if (userId == null) {
                return ResponseEntity.badRequest()
                    .body(ApiResponse.error("User not authenticated"));
            }

            byte[] pdfBytes = pdfExportService.generateGroupReport(groupId, userId);

            if (download) {
                // Return PDF file for direct download
                String filename = "group_report_" + groupId + "_" +
                    LocalDateTime.now().format(FILENAME_FORMATTER) + ".pdf";

                HttpHeaders headers = new HttpHeaders();
                headers.setContentType(MediaType.APPLICATION_PDF);
                headers.setContentDispositionFormData("attachment", filename);
                headers.setContentLength(pdfBytes.length);

                return ResponseEntity.ok()
                    .headers(headers)
                    .body(new ByteArrayResource(pdfBytes));
            } else {
                // Return download URL (as requested by user for mobile compatibility)
                String downloadUrl = "/splits/export/groups/" + groupId + "/pdf?download=true";
                PdfExportResponse response = new PdfExportResponse(downloadUrl, pdfBytes.length);
                return ResponseEntity.ok(ApiResponse.success(response));
            }

        } catch (Exception e) {
            return ResponseEntity.badRequest()
                .body(ApiResponse.error(e.getMessage()));
        }
    }

    /**
     * Export individual user expense report as PDF
     */
    @GetMapping("/individual/pdf")
    public ResponseEntity<?> exportIndividualReport(@RequestParam(defaultValue = "false") boolean download,
                                                   HttpServletRequest httpRequest) {
        try {
            Long userId = (Long) httpRequest.getAttribute("userId");
            if (userId == null) {
                return ResponseEntity.badRequest()
                    .body(ApiResponse.error("User not authenticated"));
            }

            byte[] pdfBytes = pdfExportService.generateIndividualReport(userId);

            if (download) {
                String filename = "individual_report_" + userId + "_" +
                    LocalDateTime.now().format(FILENAME_FORMATTER) + ".pdf";

                HttpHeaders headers = new HttpHeaders();
                headers.setContentType(MediaType.APPLICATION_PDF);
                headers.setContentDispositionFormData("attachment", filename);
                headers.setContentLength(pdfBytes.length);

                return ResponseEntity.ok()
                    .headers(headers)
                    .body(new ByteArrayResource(pdfBytes));
            } else {
                String downloadUrl = "/splits/export/individual/pdf?download=true";
                PdfExportResponse response = new PdfExportResponse(downloadUrl, pdfBytes.length);
                return ResponseEntity.ok(ApiResponse.success(response));
            }

        } catch (Exception e) {
            return ResponseEntity.badRequest()
                .body(ApiResponse.error(e.getMessage()));
        }
    }

    /**
     * Export friend expense report as PDF
     */
    @GetMapping("/friends/{friendId}/pdf")
    public ResponseEntity<?> exportFriendReport(@PathVariable Long friendId,
                                              @RequestParam(defaultValue = "false") boolean download,
                                              HttpServletRequest httpRequest) {
        try {
            Long userId = (Long) httpRequest.getAttribute("userId");
            if (userId == null) {
                return ResponseEntity.badRequest()
                    .body(ApiResponse.error("User not authenticated"));
            }

            byte[] pdfBytes = pdfExportService.generateFriendReport(userId, friendId);

            if (download) {
                String filename = "friend_report_" + userId + "_" + friendId + "_" +
                    LocalDateTime.now().format(FILENAME_FORMATTER) + ".pdf";

                HttpHeaders headers = new HttpHeaders();
                headers.setContentType(MediaType.APPLICATION_PDF);
                headers.setContentDispositionFormData("attachment", filename);
                headers.setContentLength(pdfBytes.length);

                return ResponseEntity.ok()
                    .headers(headers)
                    .body(new ByteArrayResource(pdfBytes));
            } else {
                String downloadUrl = "/splits/export/friends/" + friendId + "/pdf?download=true";
                PdfExportResponse response = new PdfExportResponse(downloadUrl, pdfBytes.length);
                return ResponseEntity.ok(ApiResponse.success(response));
            }

        } catch (Exception e) {
            return ResponseEntity.badRequest()
                .body(ApiResponse.error(e.getMessage()));
        }
    }

    /**
     * Get available export formats and types
     */
    @GetMapping("/formats")
    public ResponseEntity<ApiResponse<ExportFormatsResponse>> getExportFormats(HttpServletRequest httpRequest) {
        try {
            Long userId = (Long) httpRequest.getAttribute("userId");
            if (userId == null) {
                return ResponseEntity.badRequest()
                    .body(ApiResponse.error("User not authenticated"));
            }

            ExportFormatsResponse response = new ExportFormatsResponse();
            return ResponseEntity.ok(ApiResponse.success(response));

        } catch (Exception e) {
            return ResponseEntity.badRequest()
                .body(ApiResponse.error(e.getMessage()));
        }
    }

    // Response DTOs
    public static class PdfExportResponse {
        private String downloadUrl;
        private long fileSizeBytes;
        private String format = "PDF";
        private String generatedAt;

        public PdfExportResponse(String downloadUrl, long fileSizeBytes) {
            this.downloadUrl = downloadUrl;
            this.fileSizeBytes = fileSizeBytes;
            this.generatedAt = LocalDateTime.now().format(DateTimeFormatter.ISO_LOCAL_DATE_TIME);
        }

        public String getDownloadUrl() { return downloadUrl; }
        public void setDownloadUrl(String downloadUrl) { this.downloadUrl = downloadUrl; }

        public long getFileSizeBytes() { return fileSizeBytes; }
        public void setFileSizeBytes(long fileSizeBytes) { this.fileSizeBytes = fileSizeBytes; }

        public String getFormat() { return format; }
        public void setFormat(String format) { this.format = format; }

        public String getGeneratedAt() { return generatedAt; }
        public void setGeneratedAt(String generatedAt) { this.generatedAt = generatedAt; }
    }

    public static class ExportFormatsResponse {
        private String[] availableFormats = {"PDF"};
        private String[] availableReportTypes = {"group", "individual", "friend"};
        private ExportOptionInfo[] reportTypes;

        public ExportFormatsResponse() {
            this.reportTypes = new ExportOptionInfo[]{
                new ExportOptionInfo("group", "Group Report",
                    "Comprehensive report for a specific group including members, expenses, and balances"),
                new ExportOptionInfo("individual", "Individual Report",
                    "Personal report showing all your friends and group balances, recent expenses, and overall summary"),
                new ExportOptionInfo("friend", "Friend Report",
                    "Report showing shared expenses and balances between you and a specific friend")
            };
        }

        public String[] getAvailableFormats() { return availableFormats; }
        public void setAvailableFormats(String[] availableFormats) { this.availableFormats = availableFormats; }

        public String[] getAvailableReportTypes() { return availableReportTypes; }
        public void setAvailableReportTypes(String[] availableReportTypes) { this.availableReportTypes = availableReportTypes; }

        public ExportOptionInfo[] getReportTypes() { return reportTypes; }
        public void setReportTypes(ExportOptionInfo[] reportTypes) { this.reportTypes = reportTypes; }
    }

    public static class ExportOptionInfo {
        private String type;
        private String name;
        private String description;

        public ExportOptionInfo(String type, String name, String description) {
            this.type = type;
            this.name = name;
            this.description = description;
        }

        public String getType() { return type; }
        public void setType(String type) { this.type = type; }

        public String getName() { return name; }
        public void setName(String name) { this.name = name; }

        public String getDescription() { return description; }
        public void setDescription(String description) { this.description = description; }
    }
}