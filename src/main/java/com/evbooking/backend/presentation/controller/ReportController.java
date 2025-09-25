package com.evbooking.backend.presentation.controller;

import com.evbooking.backend.presentation.dto.*;
import com.evbooking.backend.usecase.service.ReportService;
import com.evbooking.backend.usecase.service.PdfReportService;
import com.evbooking.backend.usecase.service.TempPdfStorageService;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;

@RestController
@RequestMapping("/reports")
public class ReportController {

    private final ReportService reportService;
    private final PdfReportService pdfReportService;
    private final TempPdfStorageService tempPdfStorageService;

    public ReportController(ReportService reportService, PdfReportService pdfReportService, TempPdfStorageService tempPdfStorageService) {
        this.reportService = reportService;
        this.pdfReportService = pdfReportService;
        this.tempPdfStorageService = tempPdfStorageService;
    }

    @GetMapping("/overall/{bookId}")
    public ResponseEntity<ApiResponse<OverallReportResponse>> getOverallReport(@PathVariable Long bookId,
                                                                              HttpServletRequest httpRequest) {
        try {
            Long userId = (Long) httpRequest.getAttribute("userId");
            if (userId == null) {
                return ResponseEntity.badRequest()
                    .body(ApiResponse.error("User not authenticated"));
            }

            ReportService.OverallReport report = reportService.getOverallReport(bookId, userId);

            OverallReportResponse response = new OverallReportResponse(
                report.getTotalExpense(),
                report.getTotalIncome(),
                report.getBalance()
            );

            return ResponseEntity.ok(ApiResponse.success(response));

        } catch (Exception e) {
            return ResponseEntity.badRequest()
                .body(ApiResponse.error(e.getMessage()));
        }
    }

    @GetMapping("/date-range/{bookId}")
    public ResponseEntity<ApiResponse<DateRangeReportResponse>> getDateRangeReport(
            @PathVariable Long bookId,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime startDate,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime endDate,
            HttpServletRequest httpRequest) {
        try {
            Long userId = (Long) httpRequest.getAttribute("userId");
            if (userId == null) {
                return ResponseEntity.badRequest()
                    .body(ApiResponse.error("User not authenticated"));
            }

            ReportService.DateRangeReport report = reportService.getDateRangeReport(bookId, startDate, endDate, userId);

            DateRangeReportResponse response = new DateRangeReportResponse(
                report.getTotalExpense(),
                report.getTotalIncome(),
                report.getBalance(),
                report.getStartDate(),
                report.getEndDate()
            );

            return ResponseEntity.ok(ApiResponse.success(response));

        } catch (Exception e) {
            return ResponseEntity.badRequest()
                .body(ApiResponse.error(e.getMessage()));
        }
    }

    @GetMapping("/user/date-range")
    public ResponseEntity<ApiResponse<UserDateRangeReportResponse>> getUserDateRangeReport(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime startDate,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime endDate,
            HttpServletRequest httpRequest) {
        try {
            Long userId = (Long) httpRequest.getAttribute("userId");
            if (userId == null) {
                return ResponseEntity.badRequest()
                    .body(ApiResponse.error("User not authenticated"));
            }

            ReportService.UserDateRangeReport report = reportService.getUserDateRangeReport(userId, startDate, endDate);

            UserDateRangeReportResponse response = new UserDateRangeReportResponse(
                report.getTotalExpense(),
                report.getTotalIncome(),
                report.getBalance(),
                report.getStartDate(),
                report.getEndDate()
            );

            return ResponseEntity.ok(ApiResponse.success(response));

        } catch (Exception e) {
            return ResponseEntity.badRequest()
                .body(ApiResponse.error(e.getMessage()));
        }
    }

    @GetMapping("/user/overall")
    public ResponseEntity<ApiResponse<UserOverallReportResponse>> getUserOverallReport(HttpServletRequest httpRequest) {
        try {
            Long userId = (Long) httpRequest.getAttribute("userId");
            if (userId == null) {
                return ResponseEntity.badRequest()
                    .body(ApiResponse.error("User not authenticated"));
            }

            ReportService.UserOverallReport report = reportService.getUserOverallReport(userId);

            UserOverallReportResponse response = new UserOverallReportResponse(
                report.getTotalExpense(),
                report.getTotalIncome(),
                report.getBalance(),
                report.getCurrentMonthSavings()
            );

            return ResponseEntity.ok(ApiResponse.success(response));

        } catch (Exception e) {
            return ResponseEntity.badRequest()
                .body(ApiResponse.error(e.getMessage()));
        }
    }

    @GetMapping("/pdf-url/book/{bookId}")
    public ResponseEntity<ApiResponse<PdfDownloadResponse>> getBookPdfDownloadUrl(@PathVariable Long bookId, HttpServletRequest httpRequest) {
        try {
            Long userId = (Long) httpRequest.getAttribute("userId");
            if (userId == null) {
                return ResponseEntity.badRequest()
                    .body(ApiResponse.error("User not authenticated"));
            }

            byte[] pdfBytes = pdfReportService.generateBookReport(bookId, userId);
            String fileName = "book_report_" + bookId + ".pdf";

            TempPdfStorageService.TempPdfInfo pdfInfo = tempPdfStorageService.storePdf(pdfBytes, fileName, userId);
            String downloadUrl = "/reports/download/" + pdfInfo.getToken();

            PdfDownloadResponse response = new PdfDownloadResponse(downloadUrl, fileName, pdfInfo.getExpiresAt());

            return ResponseEntity.ok(ApiResponse.success(response));

        } catch (Exception e) {
            return ResponseEntity.badRequest()
                .body(ApiResponse.error(e.getMessage()));
        }
    }

    @GetMapping("/pdf-url/date-range")
    public ResponseEntity<ApiResponse<PdfDownloadResponse>> getDateRangePdfDownloadUrl(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime startDate,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime endDate,
            HttpServletRequest httpRequest) {
        try {
            Long userId = (Long) httpRequest.getAttribute("userId");
            if (userId == null) {
                return ResponseEntity.badRequest()
                    .body(ApiResponse.error("User not authenticated"));
            }

            byte[] pdfBytes = pdfReportService.generateDateRangeReport(userId, startDate, endDate);
            String fileName = "financial_report.pdf";

            TempPdfStorageService.TempPdfInfo pdfInfo = tempPdfStorageService.storePdf(pdfBytes, fileName, userId);
            String downloadUrl = "/reports/download/" + pdfInfo.getToken();

            PdfDownloadResponse response = new PdfDownloadResponse(downloadUrl, fileName, pdfInfo.getExpiresAt());

            return ResponseEntity.ok(ApiResponse.success(response));

        } catch (Exception e) {
            return ResponseEntity.badRequest()
                .body(ApiResponse.error(e.getMessage()));
        }
    }

    @GetMapping("/download/{token}")
    public ResponseEntity<byte[]> downloadPdf(@PathVariable String token, HttpServletRequest httpRequest) {
        try {
            Long userId = (Long) httpRequest.getAttribute("userId");
            if (userId == null) {
                return ResponseEntity.badRequest().build();
            }

            TempPdfStorageService.TempPdfData pdfData = tempPdfStorageService.retrievePdf(token, userId);
            if (pdfData == null) {
                return ResponseEntity.notFound().build();
            }

            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_PDF);
            headers.setContentDispositionFormData("attachment", pdfData.getFileName());
            headers.setContentLength(pdfData.getPdfBytes().length);

            // Remove PDF from storage after download for security
            tempPdfStorageService.removePdf(token);

            return ResponseEntity.ok()
                .headers(headers)
                .body(pdfData.getPdfBytes());

        } catch (Exception e) {
            return ResponseEntity.internalServerError().build();
        }
    }
}