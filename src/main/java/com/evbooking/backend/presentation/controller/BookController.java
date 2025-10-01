package com.evbooking.backend.presentation.controller;

import com.evbooking.backend.presentation.dto.*;
import com.evbooking.backend.usecase.service.BookService;
import com.evbooking.backend.domain.model.Book;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;

@RestController
@RequestMapping("/books")
public class BookController {

    private final BookService bookService;

    public BookController(BookService bookService) {
        this.bookService = bookService;
    }

    @PostMapping
    public ResponseEntity<ApiResponse<BookResponse>> createBook(@Valid @RequestBody CreateBookRequest request,
                                                               HttpServletRequest httpRequest) {
        try {
            Long userId = (Long) httpRequest.getAttribute("userId");
            if (userId == null) {
                return ResponseEntity.badRequest()
                    .body(ApiResponse.error("User not authenticated"));
            }

            Book book = bookService.createBook(request.getName(), request.getDescription(), request.getCurrency(), userId);

            BookResponse response = new BookResponse(
                book.getId(),
                book.getName(),
                book.getDescription(),
                book.getCurrency(),
                book.getCreatedAt(),
                book.getUpdatedAt()
            );

            return ResponseEntity.ok(ApiResponse.success(response));

        } catch (Exception e) {
            return ResponseEntity.badRequest()
                .body(ApiResponse.error(e.getMessage()));
        }
    }

    @GetMapping
    public ResponseEntity<ApiResponse<PagedResponse<BookResponse>>> getBooks(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size,
            HttpServletRequest httpRequest) {
        try {
            Long userId = (Long) httpRequest.getAttribute("userId");
            if (userId == null) {
                return ResponseEntity.badRequest()
                    .body(ApiResponse.error("User not authenticated"));
            }

            Pageable pageable = PageRequest.of(page, size);
            Page<Book> bookPage = bookService.getBooksByUserId(userId, pageable);

            List<BookResponse> bookResponses = bookPage.getContent().stream()
                .map(book -> {
                    BookService.BookSummary summary = bookService.getBookSummary(book.getId(), userId);
                    return new BookResponse(
                        book.getId(),
                        book.getName(),
                        book.getDescription(),
                        book.getCurrency(),
                        book.getCreatedAt(),
                        book.getUpdatedAt(),
                        summary.getTotalExpense(),
                        summary.getTotalIncome(),
                        summary.getLastEntryDateTime()
                    );
                })
                .collect(Collectors.toList());

            PagedResponse<BookResponse> response = new PagedResponse<>(
                bookResponses,
                bookPage.getNumber(),
                bookPage.getSize(),
                bookPage.getTotalElements(),
                bookPage.getTotalPages(),
                bookPage.isFirst(),
                bookPage.isLast()
            );

            return ResponseEntity.ok(ApiResponse.success(response));

        } catch (Exception e) {
            return ResponseEntity.badRequest()
                .body(ApiResponse.error(e.getMessage()));
        }
    }

    @DeleteMapping("/{bookId}")
    public ResponseEntity<ApiResponse<String>> deleteBook(@PathVariable Long bookId,
                                                         HttpServletRequest httpRequest) {
        try {
            Long userId = (Long) httpRequest.getAttribute("userId");
            if (userId == null) {
                return ResponseEntity.badRequest()
                    .body(ApiResponse.error("User not authenticated"));
            }

            bookService.deleteBook(bookId, userId);
            return ResponseEntity.ok(ApiResponse.success("Book deleted successfully"));

        } catch (Exception e) {
            return ResponseEntity.badRequest()
                .body(ApiResponse.error(e.getMessage()));
        }
    }

    @GetMapping("/{bookId}")
    public ResponseEntity<ApiResponse<BookResponse>> getBook(@PathVariable Long bookId,
                                                           HttpServletRequest httpRequest) {
        try {
            Long userId = (Long) httpRequest.getAttribute("userId");
            if (userId == null) {
                return ResponseEntity.badRequest()
                    .body(ApiResponse.error("User not authenticated"));
            }

            Optional<Book> bookOpt = bookService.getBookById(bookId);
            if (bookOpt.isEmpty()) {
                return ResponseEntity.badRequest()
                    .body(ApiResponse.error("Book not found"));
            }

            Book book = bookOpt.get();
            if (!book.getUserId().equals(userId)) {
                return ResponseEntity.badRequest()
                    .body(ApiResponse.error("You can only access your own books"));
            }

            BookService.BookSummary summary = bookService.getBookSummary(book.getId(), userId);
            BookResponse response = new BookResponse(
                book.getId(),
                book.getName(),
                book.getDescription(),
                book.getCurrency(),
                book.getCreatedAt(),
                book.getUpdatedAt(),
                summary.getTotalExpense(),
                summary.getTotalIncome(),
                summary.getLastEntryDateTime()
            );

            return ResponseEntity.ok(ApiResponse.success(response));

        } catch (Exception e) {
            return ResponseEntity.badRequest()
                .body(ApiResponse.error(e.getMessage()));
        }
    }
}