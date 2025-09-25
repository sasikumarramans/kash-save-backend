package com.evbooking.backend.presentation.controller;

import com.evbooking.backend.presentation.dto.*;
import com.evbooking.backend.usecase.service.ReportService;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;

@RestController
@RequestMapping("/reports")
public class ReportController {

    private final ReportService reportService;

    public ReportController(ReportService reportService) {
        this.reportService = reportService;
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
}