package com.evbooking.backend.presentation.controller;

import com.evbooking.backend.presentation.dto.*;
import com.evbooking.backend.usecase.service.EntryService;
import com.evbooking.backend.domain.model.Entry;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/entries")
public class EntryController {

    private final EntryService entryService;

    public EntryController(EntryService entryService) {
        this.entryService = entryService;
    }

    @PostMapping
    public ResponseEntity<ApiResponse<EntryResponse>> createEntry(@Valid @RequestBody CreateEntryRequest request,
                                                                 HttpServletRequest httpRequest) {
        try {
            Long userId = (Long) httpRequest.getAttribute("userId");
            if (userId == null) {
                return ResponseEntity.badRequest()
                    .body(ApiResponse.error("User not authenticated"));
            }

            Entry entry = entryService.createEntry(
                request.getBookId(),
                request.getType(),
                request.getName(),
                request.getAmount(),
                request.getCurrency(),
                request.getDateTime(),
                userId
            );

            EntryResponse response = new EntryResponse(
                entry.getId(),
                entry.getBookId(),
                entry.getType(),
                entry.getName(),
                entry.getAmount(),
                entry.getCurrency(),
                entry.getDateTime(),
                entry.getCreatedAt(),
                entry.getUpdatedAt()
            );

            return ResponseEntity.ok(ApiResponse.success(response));

        } catch (Exception e) {
            return ResponseEntity.badRequest()
                .body(ApiResponse.error(e.getMessage()));
        }
    }

    @PutMapping("/{entryId}")
    public ResponseEntity<ApiResponse<EntryResponse>> updateEntry(@PathVariable Long entryId,
                                                                 @Valid @RequestBody UpdateEntryRequest request,
                                                                 HttpServletRequest httpRequest) {
        try {
            Long userId = (Long) httpRequest.getAttribute("userId");
            if (userId == null) {
                return ResponseEntity.badRequest()
                    .body(ApiResponse.error("User not authenticated"));
            }

            Entry entry = entryService.updateEntry(
                entryId,
                request.getType(),
                request.getName(),
                request.getAmount(),
                request.getCurrency(),
                request.getDateTime(),
                userId
            );

            EntryResponse response = new EntryResponse(
                entry.getId(),
                entry.getBookId(),
                entry.getType(),
                entry.getName(),
                entry.getAmount(),
                entry.getCurrency(),
                entry.getDateTime(),
                entry.getCreatedAt(),
                entry.getUpdatedAt()
            );

            return ResponseEntity.ok(ApiResponse.success(response));

        } catch (Exception e) {
            return ResponseEntity.badRequest()
                .body(ApiResponse.error(e.getMessage()));
        }
    }

    @DeleteMapping("/{entryId}")
    public ResponseEntity<ApiResponse<String>> deleteEntry(@PathVariable Long entryId,
                                                          HttpServletRequest httpRequest) {
        try {
            Long userId = (Long) httpRequest.getAttribute("userId");
            if (userId == null) {
                return ResponseEntity.badRequest()
                    .body(ApiResponse.error("User not authenticated"));
            }

            entryService.deleteEntry(entryId, userId);
            return ResponseEntity.ok(ApiResponse.success("Entry deleted successfully"));

        } catch (Exception e) {
            return ResponseEntity.badRequest()
                .body(ApiResponse.error(e.getMessage()));
        }
    }

    @GetMapping("/{entryId}")
    public ResponseEntity<ApiResponse<EntryResponse>> getEntry(@PathVariable Long entryId,
                                                              HttpServletRequest httpRequest) {
        try {
            Long userId = (Long) httpRequest.getAttribute("userId");
            if (userId == null) {
                return ResponseEntity.badRequest()
                    .body(ApiResponse.error("User not authenticated"));
            }

            Optional<Entry> entryOpt = entryService.getEntryById(entryId, userId);
            if (entryOpt.isEmpty()) {
                return ResponseEntity.badRequest()
                    .body(ApiResponse.error("Entry not found"));
            }

            Entry entry = entryOpt.get();
            EntryResponse response = new EntryResponse(
                entry.getId(),
                entry.getBookId(),
                entry.getType(),
                entry.getName(),
                entry.getAmount(),
                entry.getCurrency(),
                entry.getDateTime(),
                entry.getCreatedAt(),
                entry.getUpdatedAt()
            );

            return ResponseEntity.ok(ApiResponse.success(response));

        } catch (Exception e) {
            return ResponseEntity.badRequest()
                .body(ApiResponse.error(e.getMessage()));
        }
    }

    @GetMapping
    public ResponseEntity<ApiResponse<PagedResponse<EntryResponse>>> getEntries(
            @RequestParam Long bookId,
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
            Page<Entry> entryPage = entryService.getEntriesByBookId(bookId, userId, pageable);

            List<EntryResponse> entryResponses = entryPage.getContent().stream()
                .map(entry -> new EntryResponse(
                    entry.getId(),
                    entry.getBookId(),
                    entry.getType(),
                    entry.getName(),
                    entry.getAmount(),
                    entry.getCurrency(),
                    entry.getDateTime(),
                    entry.getCreatedAt(),
                    entry.getUpdatedAt()
                ))
                .collect(Collectors.toList());

            PagedResponse<EntryResponse> response = new PagedResponse<>(
                entryResponses,
                entryPage.getNumber(),
                entryPage.getSize(),
                entryPage.getTotalElements(),
                entryPage.getTotalPages(),
                entryPage.isFirst(),
                entryPage.isLast()
            );

            return ResponseEntity.ok(ApiResponse.success(response));

        } catch (Exception e) {
            return ResponseEntity.badRequest()
                .body(ApiResponse.error(e.getMessage()));
        }
    }
}