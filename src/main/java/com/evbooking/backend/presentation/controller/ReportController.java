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
            String userId = (String) httpRequest.getAttribute("userId");
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
            String userId = (String) httpRequest.getAttribute("userId");
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
            String userId = (String) httpRequest.getAttribute("userId");
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
            String userId = (String) httpRequest.getAttribute("userId");
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

    // Category-based summary report for user with time period parameter
    @GetMapping("/user/summary")
    public ResponseEntity<ApiResponse<CategoryReportResponse>> getUserCategorySummary(
            @RequestParam String period,
            HttpServletRequest httpRequest) {
        try {
            String userId = (String) httpRequest.getAttribute("userId");
            if (userId == null) {
                return ResponseEntity.badRequest()
                    .body(ApiResponse.error("User not authenticated"));
            }

            LocalDateTime now = LocalDateTime.now();
            LocalDateTime startDate;

            switch (period.toLowerCase()) {
                case "this_month":
                    startDate = now.withDayOfMonth(1).withHour(0).withMinute(0).withSecond(0).withNano(0);
                    break;
                case "30_days":
                    startDate = now.minusDays(30);
                    break;
                case "60_days":
                    startDate = now.minusDays(60);
                    break;
                case "90_days":
                    startDate = now.minusDays(90);
                    break;
                case "6_months":
                    startDate = now.minusMonths(6);
                    break;
                default:
                    return ResponseEntity.badRequest()
                        .body(ApiResponse.error("Invalid period. Valid values: this_month, 30_days, 60_days, 90_days, 6_months"));
            }

            ReportService.CategoryReport report = reportService.getUserCategoryReport(userId, startDate, now, period);

            CategoryReportResponse response = new CategoryReportResponse(
                report.getTotalExpense(),
                report.getTotalIncome(),
                report.getBalance(),
                report.getStartDate(),
                report.getEndDate(),
                report.getPeriod(),
                report.getExpenseCategories(),
                report.getIncomeCategories()
            );

            return ResponseEntity.ok(ApiResponse.success(response));

        } catch (Exception e) {
            return ResponseEntity.badRequest()
                .body(ApiResponse.error(e.getMessage()));
        }
    }

    // Category-based summary report for specific book with time period parameter
    @GetMapping("/book/{bookId}/summary")
    public ResponseEntity<ApiResponse<CategoryReportResponse>> getBookCategorySummary(
            @PathVariable Long bookId,
            @RequestParam String period,
            HttpServletRequest httpRequest) {
        try {
            String userId = (String) httpRequest.getAttribute("userId");
            if (userId == null) {
                return ResponseEntity.badRequest()
                    .body(ApiResponse.error("User not authenticated"));
            }

            LocalDateTime now = LocalDateTime.now();
            LocalDateTime startDate;

            switch (period.toLowerCase()) {
                case "this_month":
                    startDate = now.withDayOfMonth(1).withHour(0).withMinute(0).withSecond(0).withNano(0);
                    break;
                case "30_days":
                    startDate = now.minusDays(30);
                    break;
                case "60_days":
                    startDate = now.minusDays(60);
                    break;
                case "90_days":
                    startDate = now.minusDays(90);
                    break;
                case "6_months":
                    startDate = now.minusMonths(6);
                    break;
                default:
                    return ResponseEntity.badRequest()
                        .body(ApiResponse.error("Invalid period. Valid values: this_month, 30_days, 60_days, 90_days, 6_months"));
            }

            ReportService.CategoryReport report = reportService.getBookCategoryReport(bookId, userId, startDate, now, period);

            CategoryReportResponse response = new CategoryReportResponse(
                report.getTotalExpense(),
                report.getTotalIncome(),
                report.getBalance(),
                report.getStartDate(),
                report.getEndDate(),
                report.getPeriod(),
                report.getExpenseCategories(),
                report.getIncomeCategories()
            );

            return ResponseEntity.ok(ApiResponse.success(response));

        } catch (Exception e) {
            return ResponseEntity.badRequest()
                .body(ApiResponse.error(e.getMessage()));
        }
    }

    // PDF download for user category summary
    @GetMapping("/pdf-url/user/summary")
    public ResponseEntity<ApiResponse<PdfDownloadResponse>> getUserSummaryPdfDownloadUrl(
            @RequestParam String period,
            HttpServletRequest httpRequest) {
        try {
            String userId = (String) httpRequest.getAttribute("userId");
            if (userId == null) {
                return ResponseEntity.badRequest()
                    .body(ApiResponse.error("User not authenticated"));
            }

            LocalDateTime now = LocalDateTime.now();
            LocalDateTime startDate;

            switch (period.toLowerCase()) {
                case "this_month":
                    startDate = now.withDayOfMonth(1).withHour(0).withMinute(0).withSecond(0).withNano(0);
                    break;
                case "30_days":
                    startDate = now.minusDays(30);
                    break;
                case "60_days":
                    startDate = now.minusDays(60);
                    break;
                case "90_days":
                    startDate = now.minusDays(90);
                    break;
                case "6_months":
                    startDate = now.minusMonths(6);
                    break;
                default:
                    return ResponseEntity.badRequest()
                        .body(ApiResponse.error("Invalid period. Valid values: this_month, 30_days, 60_days, 90_days, 6_months"));
            }

            byte[] pdfBytes = pdfReportService.generateCategorySummaryReport(userId, startDate, now, period);
            String fileName = "category_summary_" + period + ".pdf";

            TempPdfStorageService.TempPdfInfo pdfInfo = tempPdfStorageService.storePdf(pdfBytes, fileName, userId);
            String downloadUrl = "/reports/download/" + pdfInfo.getToken();

            PdfDownloadResponse response = new PdfDownloadResponse(downloadUrl, fileName, pdfInfo.getExpiresAt());

            return ResponseEntity.ok(ApiResponse.success(response));

        } catch (Exception e) {
            return ResponseEntity.badRequest()
                .body(ApiResponse.error(e.getMessage()));
        }
    }

    // PDF download for book category summary
    @GetMapping("/pdf-url/book/{bookId}/summary")
    public ResponseEntity<ApiResponse<PdfDownloadResponse>> getBookSummaryPdfDownloadUrl(
            @PathVariable Long bookId,
            @RequestParam String period,
            HttpServletRequest httpRequest) {
        try {
            String userId = (String) httpRequest.getAttribute("userId");
            if (userId == null) {
                return ResponseEntity.badRequest()
                    .body(ApiResponse.error("User not authenticated"));
            }

            LocalDateTime now = LocalDateTime.now();
            LocalDateTime startDate;

            switch (period.toLowerCase()) {
                case "this_month":
                    startDate = now.withDayOfMonth(1).withHour(0).withMinute(0).withSecond(0).withNano(0);
                    break;
                case "30_days":
                    startDate = now.minusDays(30);
                    break;
                case "60_days":
                    startDate = now.minusDays(60);
                    break;
                case "90_days":
                    startDate = now.minusDays(90);
                    break;
                case "6_months":
                    startDate = now.minusMonths(6);
                    break;
                default:
                    return ResponseEntity.badRequest()
                        .body(ApiResponse.error("Invalid period. Valid values: this_month, 30_days, 60_days, 90_days, 6_months"));
            }

            byte[] pdfBytes = pdfReportService.generateBookCategorySummaryReport(bookId, userId, startDate, now, period);
            String fileName = "book_" + bookId + "_summary_" + period + ".pdf";

            TempPdfStorageService.TempPdfInfo pdfInfo = tempPdfStorageService.storePdf(pdfBytes, fileName, userId);
            String downloadUrl = "/reports/download/" + pdfInfo.getToken();

            PdfDownloadResponse response = new PdfDownloadResponse(downloadUrl, fileName, pdfInfo.getExpiresAt());

            return ResponseEntity.ok(ApiResponse.success(response));

        } catch (Exception e) {
            return ResponseEntity.badRequest()
                .body(ApiResponse.error(e.getMessage()));
        }
    }

    @GetMapping("/pdf-url/book/{bookId}")
    public ResponseEntity<ApiResponse<PdfDownloadResponse>> getBookPdfDownloadUrl(@PathVariable Long bookId, HttpServletRequest httpRequest) {
        try {
            String userId = (String) httpRequest.getAttribute("userId");
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
            String userId = (String) httpRequest.getAttribute("userId");
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
            String userId = (String) httpRequest.getAttribute("userId");
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