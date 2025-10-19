package com.evbooking.backend.presentation.controller.split;

import com.evbooking.backend.domain.model.split.Settlement;
import com.evbooking.backend.presentation.dto.ApiResponse;
import com.evbooking.backend.presentation.dto.split.CreateSettlementRequest;
import com.evbooking.backend.presentation.dto.split.SettlementResponse;
import com.evbooking.backend.usecase.service.split.SettlementService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/splits")
public class SettlementController {

    private final SettlementService settlementService;

    public SettlementController(SettlementService settlementService) {
        this.settlementService = settlementService;
    }

    /**
     * Create a standalone settlement (person-to-person payment)
     * Example: Bob paid ₹100 to Charlie directly
     */
    @PostMapping("/settlements")
    public ResponseEntity<ApiResponse<SettlementResponse>> createStandaloneSettlement(
            @Valid @RequestBody CreateSettlementRequest request,
            HttpServletRequest servletRequest) {
        try {
            String userId = (String) servletRequest.getAttribute("userId");
            if (userId == null) {
                return ResponseEntity.badRequest()
                    .body(ApiResponse.error("User not authenticated"));
            }

            Settlement settlement = settlementService.createStandaloneSettlement(
                request.getFromUsername(),
                request.getToUsername(),
                request.getAmount(),
                request.getCurrency(),
                request.getGroupId(),
                request.getGroupType(),
                request.getNotes(),
                userId
            );

            // Convert to response using the service method
            Page<SettlementResponse> page = settlementService.getUserSettlements(
                userId,
                PageRequest.of(0, 1)
            );

            // Find the created settlement in the response
            SettlementResponse response = page.getContent().stream()
                .filter(s -> s.getId().equals(settlement.getId()))
                .findFirst()
                .orElse(null);

            if (response == null) {
                // Fallback: get settlements and find it
                Page<SettlementResponse> allSettlements = settlementService.getUserSettlements(
                    settlement.getFromUserId(),
                    PageRequest.of(0, 10, Sort.by(Sort.Direction.DESC, "settlementDate"))
                );
                response = allSettlements.getContent().stream()
                    .filter(s -> s.getId().equals(settlement.getId()))
                    .findFirst()
                    .orElseThrow(() -> new RuntimeException("Settlement created but not found"));
            }

            return ResponseEntity.ok(ApiResponse.success(response));

        } catch (Exception e) {
            return ResponseEntity.badRequest()
                .body(ApiResponse.error(e.getMessage()));
        }
    }

    /**
     * Create a standalone settlement within a specific group
     * Example: Bob paid ₹100 to Charlie in "Roommates" group
     */
    @PostMapping("/groups/{groupId}/settlements")
    public ResponseEntity<ApiResponse<SettlementResponse>> createGroupSettlement(
            @PathVariable Long groupId,
            @Valid @RequestBody CreateSettlementRequest request,
            HttpServletRequest servletRequest) {
        try {
            String userId = (String) servletRequest.getAttribute("userId");
            if (userId == null) {
                return ResponseEntity.badRequest()
                    .body(ApiResponse.error("User not authenticated"));
            }

            // Override groupId from path parameter
            request.setGroupId(groupId);

            Settlement settlement = settlementService.createStandaloneSettlement(
                request.getFromUsername(),
                request.getToUsername(),
                request.getAmount(),
                request.getCurrency(),
                groupId,
                request.getGroupType(),
                request.getNotes(),
                userId
            );

            // Get the settlement response
            Page<SettlementResponse> page = settlementService.getGroupSettlements(
                groupId,
                request.getGroupType(),
                userId,
                PageRequest.of(0, 10, Sort.by(Sort.Direction.DESC, "settlementDate"))
            );

            SettlementResponse response = page.getContent().stream()
                .filter(s -> s.getId().equals(settlement.getId()))
                .findFirst()
                .orElseThrow(() -> new RuntimeException("Settlement created but not found"));

            return ResponseEntity.ok(ApiResponse.success(response));

        } catch (Exception e) {
            return ResponseEntity.badRequest()
                .body(ApiResponse.error(e.getMessage()));
        }
    }

    /**
     * Get all settlements for the authenticated user
     */
    @GetMapping("/settlements")
    public ResponseEntity<ApiResponse<Page<SettlementResponse>>> getUserSettlements(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size,
            HttpServletRequest servletRequest) {
        try {
            String userId = (String) servletRequest.getAttribute("userId");
            if (userId == null) {
                return ResponseEntity.badRequest()
                    .body(ApiResponse.error("User not authenticated"));
            }

            Pageable pageable = PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "settlementDate"));
            Page<SettlementResponse> settlements = settlementService.getUserSettlements(userId, pageable);

            return ResponseEntity.ok(ApiResponse.success(settlements));

        } catch (Exception e) {
            return ResponseEntity.badRequest()
                .body(ApiResponse.error(e.getMessage()));
        }
    }

    /**
     * Get settlements between two users
     */
    @GetMapping("/settlements/with/{friendUsername}")
    public ResponseEntity<ApiResponse<List<SettlementResponse>>> getSettlementsWithFriend(
            @PathVariable String friendUsername,
            HttpServletRequest servletRequest) {
        try {
            String userId = (String) servletRequest.getAttribute("userId");
            if (userId == null) {
                return ResponseEntity.badRequest()
                    .body(ApiResponse.error("User not authenticated"));
            }

            // This would need username-to-userId resolution in the service
            // For now, returning error - you'd need to enhance the service
            return ResponseEntity.badRequest()
                .body(ApiResponse.error("Not implemented yet - use user IDs directly"));

        } catch (Exception e) {
            return ResponseEntity.badRequest()
                .body(ApiResponse.error(e.getMessage()));
        }
    }

    /**
     * Get all settlements for a group
     */
    @GetMapping("/groups/{groupId}/settlements")
    public ResponseEntity<ApiResponse<Page<SettlementResponse>>> getGroupSettlements(
            @PathVariable Long groupId,
            @RequestParam(required = false) String groupType,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size,
            HttpServletRequest servletRequest) {
        try {
            String userId = (String) servletRequest.getAttribute("userId");
            if (userId == null) {
                return ResponseEntity.badRequest()
                    .body(ApiResponse.error("User not authenticated"));
            }

            Pageable pageable = PageRequest.of(page, size);
            Page<SettlementResponse> settlements = settlementService.getGroupSettlements(groupId, groupType, userId, pageable);

            return ResponseEntity.ok(ApiResponse.success(settlements));

        } catch (Exception e) {
            return ResponseEntity.badRequest()
                .body(ApiResponse.error(e.getMessage()));
        }
    }
}
