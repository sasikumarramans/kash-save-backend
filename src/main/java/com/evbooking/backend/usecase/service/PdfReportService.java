package com.evbooking.backend.usecase.service;

import com.evbooking.backend.domain.model.Entry;
import com.evbooking.backend.domain.model.Book;
import com.evbooking.backend.domain.repository.EntryRepository;
import com.evbooking.backend.presentation.dto.CategorySummary;
import com.itextpdf.kernel.pdf.PdfDocument;
import com.itextpdf.kernel.pdf.PdfWriter;
import com.itextpdf.layout.Document;
import com.itextpdf.layout.element.Paragraph;
import com.itextpdf.layout.element.Table;
import com.itextpdf.layout.element.Cell;
import com.itextpdf.layout.properties.TextAlignment;
import com.itextpdf.layout.properties.UnitValue;
import org.springframework.stereotype.Service;

import java.io.ByteArrayOutputStream;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;

@Service
public class PdfReportService {

    private final EntryRepository entryRepository;
    private final BookService bookService;
    private final ReportService reportService;

    public PdfReportService(EntryRepository entryRepository, BookService bookService, ReportService reportService) {
        this.entryRepository = entryRepository;
        this.bookService = bookService;
        this.reportService = reportService;
    }

    public byte[] generateBookReport(Long bookId, String userId) throws Exception {
        // Verify book ownership
        if (!bookService.verifyBookOwnership(bookId, userId)) {
            throw new RuntimeException("You can only generate reports for your own books");
        }

        // Get book details
        Book book = bookService.getBookById(bookId)
            .orElseThrow(() -> new RuntimeException("Book not found"));

        // Get all entries for the book
        List<Entry> entries = entryRepository.findByBookId(bookId);

        // Calculate totals
        BigDecimal totalExpense = entryRepository.getTotalExpensesByBookId(bookId);
        BigDecimal totalIncome = entryRepository.getTotalIncomeByBookId(bookId);
        BigDecimal balance = totalIncome.subtract(totalExpense);

        return generatePdfReport(
            book.getName() + " - Complete Report",
            entries,
            totalExpense,
            totalIncome,
            balance
        );
    }

    public byte[] generateDateRangeReport(String userId, LocalDateTime startDate, LocalDateTime endDate) throws Exception {
        if (startDate == null || endDate == null) {
            throw new RuntimeException("Start date and end date are required");
        }

        if (startDate.isAfter(endDate)) {
            throw new RuntimeException("Start date cannot be after end date");
        }

        // Get all entries for user in date range
        List<Entry> entries = entryRepository.findByUserIdAndDateTimeBetween(userId, startDate, endDate);

        // Calculate totals
        BigDecimal totalExpense = entryRepository.getTotalExpensesByUserIdAndDateRange(userId, startDate, endDate);
        BigDecimal totalIncome = entryRepository.getTotalIncomeByUserIdAndDateRange(userId, startDate, endDate);
        BigDecimal balance = totalIncome.subtract(totalExpense);

        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("MMM dd, yyyy");
        String reportTitle = String.format("Financial Report (%s to %s)",
            startDate.format(formatter),
            endDate.format(formatter)
        );

        return generatePdfReport(reportTitle, entries, totalExpense, totalIncome, balance);
    }

    private byte[] generatePdfReport(String title, List<Entry> entries,
                                   BigDecimal totalExpense, BigDecimal totalIncome, BigDecimal balance) throws Exception {

        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        PdfWriter writer = new PdfWriter(baos);
        PdfDocument pdfDoc = new PdfDocument(writer);
        Document document = new Document(pdfDoc);

        // Title
        Paragraph titlePara = new Paragraph(title)
            .setTextAlignment(TextAlignment.CENTER)
            .setFontSize(20)
            .setBold();
        document.add(titlePara);

        // Generated date
        Paragraph datePara = new Paragraph("Generated on: " + LocalDateTime.now().format(DateTimeFormatter.ofPattern("MMM dd, yyyy HH:mm")))
            .setTextAlignment(TextAlignment.CENTER)
            .setFontSize(10)
            .setMarginBottom(20);
        document.add(datePara);

        if (entries.isEmpty()) {
            document.add(new Paragraph("No entries found for the specified criteria."));
        } else {
            // Create table with 4 columns: Date, Purpose, Type, Amount
            Table table = new Table(UnitValue.createPercentArray(new float[]{25, 35, 20, 20}));
            table.setWidth(UnitValue.createPercentValue(100));

            // Table headers
            table.addHeaderCell(new Cell().add(new Paragraph("Date").setBold()));
            table.addHeaderCell(new Cell().add(new Paragraph("Purpose").setBold()));
            table.addHeaderCell(new Cell().add(new Paragraph("Type").setBold()));
            table.addHeaderCell(new Cell().add(new Paragraph("Amount").setBold()));

            // Add entries
            DateTimeFormatter dateFormatter = DateTimeFormatter.ofPattern("MMM dd, yyyy");
            for (Entry entry : entries) {
                table.addCell(new Cell().add(new Paragraph(entry.getDateTime().format(dateFormatter))));
                table.addCell(new Cell().add(new Paragraph(entry.getName())));
                table.addCell(new Cell().add(new Paragraph(entry.getType().name())));

                String amountText = String.format("$%.2f", entry.getAmount());
                if (entry.isExpense()) {
                    amountText = "-" + amountText;
                } else {
                    amountText = "+" + amountText;
                }
                table.addCell(new Cell().add(new Paragraph(amountText)));
            }

            document.add(table);
        }

        // Summary section
        document.add(new Paragraph("\n"));
        document.add(new Paragraph("FINANCIAL SUMMARY")
            .setTextAlignment(TextAlignment.CENTER)
            .setFontSize(16)
            .setBold()
            .setMarginTop(20));

        // Summary table
        Table summaryTable = new Table(UnitValue.createPercentArray(new float[]{50, 50}));
        summaryTable.setWidth(UnitValue.createPercentValue(60))
            .setHorizontalAlignment(com.itextpdf.layout.properties.HorizontalAlignment.CENTER);

        summaryTable.addCell(new Cell().add(new Paragraph("Total Expenses:").setBold()));
        summaryTable.addCell(new Cell().add(new Paragraph(String.format("$%.2f", totalExpense))));

        summaryTable.addCell(new Cell().add(new Paragraph("Total Income:").setBold()));
        summaryTable.addCell(new Cell().add(new Paragraph(String.format("$%.2f", totalIncome))));

        summaryTable.addCell(new Cell().add(new Paragraph("Net Balance:").setBold()));
        String balanceText = String.format("$%.2f", balance);
        if (balance.compareTo(BigDecimal.ZERO) < 0) {
            balanceText = "(" + balanceText.substring(1) + ")"; // Show negative in parentheses
        }
        summaryTable.addCell(new Cell().add(new Paragraph(balanceText).setBold()));

        document.add(summaryTable);

        document.close();
        return baos.toByteArray();
    }

    public byte[] generateCategorySummaryReport(String userId, LocalDateTime startDate, LocalDateTime endDate, String period) throws Exception {
        ReportService.CategoryReport report = reportService.getUserCategoryReport(userId, startDate, endDate, period);

        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("MMM dd, yyyy");
        String reportTitle = String.format("Category Summary Report - %s (%s to %s)",
            period.replace("_", " ").toUpperCase(),
            startDate.format(formatter),
            endDate.format(formatter)
        );

        return generateCategorySummaryPdf(reportTitle, report);
    }

    public byte[] generateBookCategorySummaryReport(Long bookId, String userId, LocalDateTime startDate, LocalDateTime endDate, String period) throws Exception {
        // Verify book ownership
        if (!bookService.verifyBookOwnership(bookId, userId)) {
            throw new RuntimeException("You can only generate reports for your own books");
        }

        Book book = bookService.getBookById(bookId)
            .orElseThrow(() -> new RuntimeException("Book not found"));

        ReportService.CategoryReport report = reportService.getBookCategoryReport(bookId, userId, startDate, endDate, period);

        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("MMM dd, yyyy");
        String reportTitle = String.format("%s - Category Summary (%s to %s)",
            book.getName(),
            startDate.format(formatter),
            endDate.format(formatter)
        );

        return generateCategorySummaryPdf(reportTitle, report);
    }

    private byte[] generateCategorySummaryPdf(String title, ReportService.CategoryReport report) throws Exception {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        PdfWriter writer = new PdfWriter(baos);
        PdfDocument pdfDoc = new PdfDocument(writer);
        Document document = new Document(pdfDoc);

        // Title
        Paragraph titlePara = new Paragraph(title)
            .setTextAlignment(TextAlignment.CENTER)
            .setFontSize(20)
            .setBold();
        document.add(titlePara);

        // Generated date
        Paragraph datePara = new Paragraph("Generated on: " + LocalDateTime.now().format(DateTimeFormatter.ofPattern("MMM dd, yyyy HH:mm")))
            .setTextAlignment(TextAlignment.CENTER)
            .setFontSize(10)
            .setMarginBottom(20);
        document.add(datePara);

        // Overall Summary
        document.add(new Paragraph("FINANCIAL OVERVIEW")
            .setTextAlignment(TextAlignment.CENTER)
            .setFontSize(16)
            .setBold()
            .setMarginTop(10));

        Table overviewTable = new Table(UnitValue.createPercentArray(new float[]{50, 50}));
        overviewTable.setWidth(UnitValue.createPercentValue(60))
            .setHorizontalAlignment(com.itextpdf.layout.properties.HorizontalAlignment.CENTER);

        overviewTable.addCell(new Cell().add(new Paragraph("Total Expenses:").setBold()));
        overviewTable.addCell(new Cell().add(new Paragraph(String.format("$%.2f", report.getTotalExpense()))));

        overviewTable.addCell(new Cell().add(new Paragraph("Total Income:").setBold()));
        overviewTable.addCell(new Cell().add(new Paragraph(String.format("$%.2f", report.getTotalIncome()))));

        overviewTable.addCell(new Cell().add(new Paragraph("Net Balance:").setBold()));
        String balanceText = String.format("$%.2f", report.getBalance());
        if (report.getBalance().compareTo(BigDecimal.ZERO) < 0) {
            balanceText = "(" + balanceText.substring(1) + ")";
        }
        overviewTable.addCell(new Cell().add(new Paragraph(balanceText).setBold()));

        document.add(overviewTable);

        // Expense Categories
        if (!report.getExpenseCategories().isEmpty()) {
            document.add(new Paragraph("\n\nEXPENSE BREAKDOWN BY CATEGORY")
                .setTextAlignment(TextAlignment.LEFT)
                .setFontSize(14)
                .setBold()
                .setMarginTop(20));

            Table expenseTable = new Table(UnitValue.createPercentArray(new float[]{40, 20, 20, 20}));
            expenseTable.setWidth(UnitValue.createPercentValue(100));

            expenseTable.addHeaderCell(new Cell().add(new Paragraph("Category").setBold()));
            expenseTable.addHeaderCell(new Cell().add(new Paragraph("Amount").setBold()));
            expenseTable.addHeaderCell(new Cell().add(new Paragraph("Percentage").setBold()));
            expenseTable.addHeaderCell(new Cell().add(new Paragraph("Transactions").setBold()));

            for (CategorySummary category : report.getExpenseCategories()) {
                expenseTable.addCell(new Cell().add(new Paragraph(category.getCategoryName())));
                expenseTable.addCell(new Cell().add(new Paragraph(String.format("$%.2f", category.getTotalAmount()))));
                expenseTable.addCell(new Cell().add(new Paragraph(String.format("%.2f%%", category.getPercentage()))));
                expenseTable.addCell(new Cell().add(new Paragraph(String.valueOf(category.getTransactionCount()))));
            }

            document.add(expenseTable);
        }

        // Income Categories
        if (!report.getIncomeCategories().isEmpty()) {
            document.add(new Paragraph("\n\nINCOME BREAKDOWN BY CATEGORY")
                .setTextAlignment(TextAlignment.LEFT)
                .setFontSize(14)
                .setBold()
                .setMarginTop(20));

            Table incomeTable = new Table(UnitValue.createPercentArray(new float[]{40, 20, 20, 20}));
            incomeTable.setWidth(UnitValue.createPercentValue(100));

            incomeTable.addHeaderCell(new Cell().add(new Paragraph("Category").setBold()));
            incomeTable.addHeaderCell(new Cell().add(new Paragraph("Amount").setBold()));
            incomeTable.addHeaderCell(new Cell().add(new Paragraph("Percentage").setBold()));
            incomeTable.addHeaderCell(new Cell().add(new Paragraph("Transactions").setBold()));

            for (CategorySummary category : report.getIncomeCategories()) {
                incomeTable.addCell(new Cell().add(new Paragraph(category.getCategoryName())));
                incomeTable.addCell(new Cell().add(new Paragraph(String.format("$%.2f", category.getTotalAmount()))));
                incomeTable.addCell(new Cell().add(new Paragraph(String.format("%.2f%%", category.getPercentage()))));
                incomeTable.addCell(new Cell().add(new Paragraph(String.valueOf(category.getTransactionCount()))));
            }

            document.add(incomeTable);
        }

        document.close();
        return baos.toByteArray();
    }
}