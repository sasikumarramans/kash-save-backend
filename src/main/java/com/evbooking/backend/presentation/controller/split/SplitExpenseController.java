package com.evbooking.backend.presentation.controller.split;

import com.evbooking.backend.domain.model.split.SplitExpense;
import com.evbooking.backend.domain.model.split.SplitParticipant;
import com.evbooking.backend.domain.model.User;
import com.evbooking.backend.domain.repository.UserRepository;
import com.evbooking.backend.presentation.dto.ApiResponse;
import com.evbooking.backend.presentation.dto.split.*;
import com.evbooking.backend.usecase.service.split.SplitExpenseService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/splits")
public class SplitExpenseController {

    private final SplitExpenseService splitExpenseService;
    private final UserRepository userRepository;

    public SplitExpenseController(SplitExpenseService splitExpenseService, UserRepository userRepository) {
        this.splitExpenseService = splitExpenseService;
        this.userRepository = userRepository;
    }

    @PostMapping
    public ResponseEntity<ApiResponse<SplitExpenseResponse>> createSplitExpense(
            @Valid @RequestBody CreateSplitExpenseRequest request,
            HttpServletRequest httpRequest) {
        try {
            Long userId = (Long) httpRequest.getAttribute("userId");
            if (userId == null) {
                return ResponseEntity.badRequest()
                    .body(ApiResponse.error("User not authenticated"));
            }

            // Convert participant usernames to split values map
            Map<String, BigDecimal> participantSplitValues = new HashMap<>();
            for (CreateSplitExpenseRequest.SplitParticipantRequest participant : request.getParticipants()) {
                participantSplitValues.put(participant.getUsername(), participant.getSplitValue());
            }

            SplitExpense splitExpense = splitExpenseService.createSplitExpense(
                request.getDescription(),
                request.getTotalAmount(),
                request.getCurrency(),
                request.getPaidByUsername(),
                request.getGroupId(),
                request.getSplitType(),
                participantSplitValues,
                userId
            );

            SplitExpenseResponse response = convertToSplitExpenseResponse(splitExpense);
            return ResponseEntity.ok(ApiResponse.success(response));

        } catch (Exception e) {
            return ResponseEntity.badRequest()
                .body(ApiResponse.error(e.getMessage()));
        }
    }

    @GetMapping
    public ResponseEntity<ApiResponse<Page<SplitExpenseResponse>>> getUserSplitExpenses(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size,
            @RequestParam(defaultValue = "createdAt") String sortBy,
            @RequestParam(defaultValue = "desc") String sortDir,
            @RequestParam(required = false) String type, // "group", "individual", or null for all
            HttpServletRequest httpRequest) {
        try {
            Long userId = (Long) httpRequest.getAttribute("userId");
            if (userId == null) {
                return ResponseEntity.badRequest()
                    .body(ApiResponse.error("User not authenticated"));
            }

            Sort sort = Sort.by(sortDir.equalsIgnoreCase("desc") ?
                Sort.Direction.DESC : Sort.Direction.ASC, sortBy);
            Pageable pageable = PageRequest.of(page, size, sort);

            Page<SplitExpense> expenses;
            if ("individual".equals(type)) {
                expenses = splitExpenseService.getIndividualSplitExpenses(userId, pageable);
            } else {
                expenses = splitExpenseService.getUserSplitExpenses(userId, pageable);
            }

            Page<SplitExpenseResponse> response = expenses.map(this::convertToSplitExpenseResponse);
            return ResponseEntity.ok(ApiResponse.success(response));

        } catch (Exception e) {
            return ResponseEntity.badRequest()
                .body(ApiResponse.error(e.getMessage()));
        }
    }

    @GetMapping("/{expenseId}")
    public ResponseEntity<ApiResponse<SplitExpenseResponse>> getSplitExpense(@PathVariable Long expenseId,
                                                                           HttpServletRequest httpRequest) {
        try {
            Long userId = (Long) httpRequest.getAttribute("userId");
            if (userId == null) {
                return ResponseEntity.badRequest()
                    .body(ApiResponse.error("User not authenticated"));
            }

            Optional<SplitExpense> expenseOpt = splitExpenseService.getSplitExpenseById(expenseId, userId);
            if (expenseOpt.isEmpty()) {
                return ResponseEntity.notFound().build();
            }

            SplitExpenseResponse response = convertToSplitExpenseResponse(expenseOpt.get());
            return ResponseEntity.ok(ApiResponse.success(response));

        } catch (Exception e) {
            return ResponseEntity.badRequest()
                .body(ApiResponse.error(e.getMessage()));
        }
    }

    @GetMapping("/{expenseId}/participants")
    public ResponseEntity<ApiResponse<List<SplitExpenseResponse.SplitParticipantResponse>>> getSplitParticipants(
            @PathVariable Long expenseId,
            HttpServletRequest httpRequest) {
        try {
            Long userId = (Long) httpRequest.getAttribute("userId");
            if (userId == null) {
                return ResponseEntity.badRequest()
                    .body(ApiResponse.error("User not authenticated"));
            }

            List<SplitParticipant> participants = splitExpenseService.getSplitParticipants(expenseId, userId);
            List<SplitExpenseResponse.SplitParticipantResponse> response = participants.stream()
                .map(this::convertToSplitParticipantResponse)
                .collect(Collectors.toList());

            return ResponseEntity.ok(ApiResponse.success(response));

        } catch (Exception e) {
            return ResponseEntity.badRequest()
                .body(ApiResponse.error(e.getMessage()));
        }
    }

    /**
     * Get group split expenses including:
     * 1. Direct group expenses (where groupId matches)
     * 2. Individual expenses between group members
     * This provides complete financial visibility within the group context
     */
    @GetMapping("/groups/{groupId}")
    public ResponseEntity<ApiResponse<Page<SplitExpenseResponse>>> getGroupSplitExpenses(
            @PathVariable Long groupId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size,
            @RequestParam(defaultValue = "createdAt") String sortBy,
            @RequestParam(defaultValue = "desc") String sortDir,
            HttpServletRequest httpRequest) {
        try {
            Long userId = (Long) httpRequest.getAttribute("userId");
            if (userId == null) {
                return ResponseEntity.badRequest()
                    .body(ApiResponse.error("User not authenticated"));
            }

            Sort sort = Sort.by(sortDir.equalsIgnoreCase("desc") ?
                Sort.Direction.DESC : Sort.Direction.ASC, sortBy);
            Pageable pageable = PageRequest.of(page, size, sort);

            Page<SplitExpense> expenses = splitExpenseService.getGroupSplitExpenses(groupId, userId, pageable);
            Page<SplitExpenseResponse> response = expenses.map(this::convertToSplitExpenseResponse);

            return ResponseEntity.ok(ApiResponse.success(response));

        } catch (Exception e) {
            return ResponseEntity.badRequest()
                .body(ApiResponse.error(e.getMessage()));
        }
    }

    @DeleteMapping("/{expenseId}")
    public ResponseEntity<ApiResponse<String>> deleteSplitExpense(@PathVariable Long expenseId,
                                                                 HttpServletRequest httpRequest) {
        try {
            Long userId = (Long) httpRequest.getAttribute("userId");
            if (userId == null) {
                return ResponseEntity.badRequest()
                    .body(ApiResponse.error("User not authenticated"));
            }

            splitExpenseService.deleteSplitExpense(expenseId, userId);
            return ResponseEntity.ok(ApiResponse.success("Split expense deleted successfully"));

        } catch (Exception e) {
            return ResponseEntity.badRequest()
                .body(ApiResponse.error(e.getMessage()));
        }
    }

    @PutMapping("/{expenseId}/participants/{participantUserId}/settlement")
    public ResponseEntity<ApiResponse<SplitExpenseResponse.SplitParticipantResponse>> updateSettlement(
            @PathVariable Long expenseId,
            @PathVariable Long participantUserId,
            @RequestBody SettlementUpdateRequest request,
            HttpServletRequest httpRequest) {
        try {
            Long userId = (Long) httpRequest.getAttribute("userId");
            if (userId == null) {
                return ResponseEntity.badRequest()
                    .body(ApiResponse.error("User not authenticated"));
            }

            SplitParticipant participant = splitExpenseService.updateParticipantSettlement(
                expenseId, participantUserId, request.isSettled(), userId);

            SplitExpenseResponse.SplitParticipantResponse response = convertToSplitParticipantResponse(participant);
            return ResponseEntity.ok(ApiResponse.success(response));

        } catch (Exception e) {
            return ResponseEntity.badRequest()
                .body(ApiResponse.error(e.getMessage()));
        }
    }

    private SplitExpenseResponse convertToSplitExpenseResponse(SplitExpense expense) {
        SplitExpenseResponse response = new SplitExpenseResponse();
        response.setId(expense.getId());
        response.setDescription(expense.getDescription());
        response.setTotalAmount(expense.getTotalAmount());
        response.setCurrency(expense.getCurrency());
        response.setPaidByUserId(expense.getPaidByUserId());
        response.setGroupId(expense.getGroupId());
        response.setSplitType(expense.getSplitType());
        response.setCreatedByUserId(expense.getCreatedByUserId());
        response.setCreatedAt(expense.getCreatedAt());

        // Get paid by user details
        Optional<User> paidByUserOpt = userRepository.findById(expense.getPaidByUserId());
        if (paidByUserOpt.isPresent()) {
            response.setPaidByUsername(paidByUserOpt.get().getUsername());
        }

        // Get created by user details
        Optional<User> createdByUserOpt = userRepository.findById(expense.getCreatedByUserId());
        if (createdByUserOpt.isPresent()) {
            response.setCreatedByUsername(createdByUserOpt.get().getUsername());
        }

        // Get participants
        List<SplitParticipant> participants = splitExpenseService.getSplitParticipants(expense.getId(), expense.getCreatedByUserId());
        List<SplitExpenseResponse.SplitParticipantResponse> participantResponses = participants.stream()
            .map(this::convertToSplitParticipantResponse)
            .collect(Collectors.toList());
        response.setParticipants(participantResponses);

        return response;
    }

    private SplitExpenseResponse.SplitParticipantResponse convertToSplitParticipantResponse(SplitParticipant participant) {
        SplitExpenseResponse.SplitParticipantResponse response = new SplitExpenseResponse.SplitParticipantResponse();
        response.setUserId(participant.getUserId());
        response.setAmountOwed(participant.getAmountOwed());
        response.setSplitValue(participant.getSplitValue());
        response.setSettled(participant.isSettled());
        response.setSettledAt(participant.getSettledAt());

        // Get user details
        Optional<User> userOpt = userRepository.findById(participant.getUserId());
        if (userOpt.isPresent()) {
            response.setUsername(userOpt.get().getUsername());
        }

        return response;
    }

    // Helper DTOs
    public static class SettlementUpdateRequest {
        private boolean settled;

        public boolean isSettled() { return settled; }
        public void setSettled(boolean settled) { this.settled = settled; }
    }
}