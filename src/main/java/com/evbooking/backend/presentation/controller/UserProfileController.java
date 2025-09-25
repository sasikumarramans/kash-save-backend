package com.evbooking.backend.presentation.controller;

import com.evbooking.backend.domain.model.User;
import com.evbooking.backend.presentation.dto.*;
import com.evbooking.backend.usecase.service.UserProfileService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;

import java.util.List;
import java.util.Optional;

@RestController
@RequestMapping("/user")
public class UserProfileController {

    private final UserProfileService userProfileService;

    public UserProfileController(UserProfileService userProfileService) {
        this.userProfileService = userProfileService;
    }

    @GetMapping("/profile")
    public ResponseEntity<ApiResponse<UserProfileResponse>> getUserProfile(HttpServletRequest request) {
        try {
            Long userId = (Long) request.getAttribute("userId");
            if (userId == null) {
                return ResponseEntity.badRequest()
                    .body(ApiResponse.error("User not authenticated"));
            }

            Optional<User> userOpt = userProfileService.getUserProfile(userId);
            if (userOpt.isEmpty()) {
                return ResponseEntity.notFound()
                    .build();
            }

            User user = userOpt.get();
            UserProfileResponse response = new UserProfileResponse(
                user.getId(),
                user.getEmail(),
                user.getFirstName(),
                user.getLastName(),
                user.getPhoneNumber(),
                user.getUsername(),
                user.getProfileImageUrl(),
                user.getRole().name(),
                user.getCreatedAt(),
                user.getUpdatedAt()
            );

            return ResponseEntity.ok(ApiResponse.success(response));

        } catch (Exception e) {
            return ResponseEntity.badRequest()
                .body(ApiResponse.error(e.getMessage()));
        }
    }

    @PutMapping("/profile")
    public ResponseEntity<ApiResponse<UserProfileResponse>> updateUserProfile(
            @RequestParam(value = "firstName", required = false) String firstName,
            @RequestParam(value = "lastName", required = false) String lastName,
            @RequestParam(value = "email", required = false) String email,
            @RequestParam(value = "username", required = false) String username,
            @RequestParam(value = "profileImage", required = false) MultipartFile profileImage,
            HttpServletRequest servletRequest) {
        try {
            Long userId = (Long) servletRequest.getAttribute("userId");
            if (userId == null) {
                return ResponseEntity.badRequest()
                    .body(ApiResponse.error("User not authenticated"));
            }

            User updatedUser = userProfileService.updateUserProfileWithFile(
                userId,
                firstName,
                lastName,
                email,
                username,
                profileImage
            );

            UserProfileResponse response = new UserProfileResponse(
                updatedUser.getId(),
                updatedUser.getEmail(),
                updatedUser.getFirstName(),
                updatedUser.getLastName(),
                updatedUser.getPhoneNumber(),
                updatedUser.getUsername(),
                updatedUser.getProfileImageUrl(),
                updatedUser.getRole().name(),
                updatedUser.getCreatedAt(),
                updatedUser.getUpdatedAt()
            );

            return ResponseEntity.ok(ApiResponse.success(response));

        } catch (Exception e) {
            return ResponseEntity.badRequest()
                .body(ApiResponse.error(e.getMessage()));
        }
    }

    @PostMapping("/generate-username")
    public ResponseEntity<ApiResponse<GenerateUsernameResponse>> generateUsername(
            @Valid @RequestBody GenerateUsernameRequest request) {
        try {
            List<String> suggestions = userProfileService.generateUsernamesSuggestions(request.getBaseName());

            GenerateUsernameResponse response = new GenerateUsernameResponse(
                suggestions,
                "Generated " + suggestions.size() + " username suggestions"
            );

            return ResponseEntity.ok(ApiResponse.success(response));

        } catch (Exception e) {
            return ResponseEntity.badRequest()
                .body(ApiResponse.error(e.getMessage()));
        }
    }

    @GetMapping("/check-username")
    public ResponseEntity<ApiResponse<Boolean>> checkUsernameAvailability(@RequestParam String username) {
        try {
            boolean isAvailable = userProfileService.isUsernameAvailable(username);
            return ResponseEntity.ok(ApiResponse.success(isAvailable));

        } catch (Exception e) {
            return ResponseEntity.badRequest()
                .body(ApiResponse.error(e.getMessage()));
        }
    }
}