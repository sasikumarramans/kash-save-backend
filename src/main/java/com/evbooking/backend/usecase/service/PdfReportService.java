package com.evbooking.backend.usecase.service;

import com.evbooking.backend.domain.model.Entry;
import com.evbooking.backend.domain.model.Book;
import com.evbooking.backend.domain.repository.EntryRepository;
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

    public PdfReportService(EntryRepository entryRepository, BookService bookService) {
        this.entryRepository = entryRepository;
        this.bookService = bookService;
    }

    public byte[] generateBookReport(Long bookId, Long userId) throws Exception {
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

    public byte[] generateDateRangeReport(Long userId, LocalDateTime startDate, LocalDateTime endDate) throws Exception {
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
}