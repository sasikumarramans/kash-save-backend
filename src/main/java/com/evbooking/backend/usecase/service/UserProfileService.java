package com.evbooking.backend.usecase.service;

import com.evbooking.backend.domain.model.User;
import com.evbooking.backend.domain.repository.UserRepository;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.Random;

@Service
public class UserProfileService {

    private final UserRepository userRepository;
    private final FileUploadService fileUploadService;
    private final Random random = new Random();

    public UserProfileService(UserRepository userRepository, FileUploadService fileUploadService) {
        this.userRepository = userRepository;
        this.fileUploadService = fileUploadService;
    }

    public Optional<User> getUserProfile(Long userId) {
        return userRepository.findById(userId);
    }

    public User updateUserProfile(Long userId, String firstName, String lastName,
                                String email, String username, String profileImageUrl) {
        Optional<User> userOpt = userRepository.findById(userId);
        if (userOpt.isEmpty()) {
            throw new RuntimeException("User not found");
        }

        User user = userOpt.get();

        // Validate username uniqueness if provided and different from current
        if (username != null && !username.trim().isEmpty() &&
            !username.equals(user.getUsername())) {
            if (userRepository.existsByUsername(username)) {
                throw new RuntimeException("Username already exists");
            }
            user.setUsername(username.trim());
        }

        // Validate email uniqueness if provided and different from current
        if (email != null && !email.trim().isEmpty() &&
            !email.equals(user.getEmail())) {
            if (userRepository.existsByEmail(email)) {
                throw new RuntimeException("Email already exists");
            }
            user.setEmail(email.trim());
        }

        // Update other fields
        if (firstName != null) {
            user.setFirstName(firstName.trim().isEmpty() ? null : firstName.trim());
        }
        if (lastName != null) {
            user.setLastName(lastName.trim().isEmpty() ? null : lastName.trim());
        }
        if (profileImageUrl != null) {
            user.setProfileImageUrl(profileImageUrl.trim().isEmpty() ? null : profileImageUrl.trim());
        }

        user.setUpdatedAt(LocalDateTime.now());
        return userRepository.save(user);
    }

    public User updateUserProfileWithFile(Long userId, String firstName, String lastName,
                                        String email, String username, MultipartFile profileImage) throws IOException {
        Optional<User> userOpt = userRepository.findById(userId);
        if (userOpt.isEmpty()) {
            throw new RuntimeException("User not found");
        }

        User user = userOpt.get();

        // Validate username uniqueness if provided and different from current
        if (username != null && !username.trim().isEmpty() &&
            !username.equals(user.getUsername())) {
            if (userRepository.existsByUsername(username)) {
                throw new RuntimeException("Username already exists");
            }
            user.setUsername(username.trim());
        }

        // Validate email uniqueness if provided and different from current
        if (email != null && !email.trim().isEmpty() &&
            !email.equals(user.getEmail())) {
            if (userRepository.existsByEmail(email)) {
                throw new RuntimeException("Email already exists");
            }
            user.setEmail(email.trim());
        }

        // Update other fields
        if (firstName != null) {
            user.setFirstName(firstName.trim().isEmpty() ? null : firstName.trim());
        }
        if (lastName != null) {
            user.setLastName(lastName.trim().isEmpty() ? null : lastName.trim());
        }

        // Handle profile image upload
        if (profileImage != null && !profileImage.isEmpty()) {
            // Delete old image if exists
            if (user.getProfileImageUrl() != null) {
                fileUploadService.deleteProfileImage(user.getProfileImageUrl());
            }

            // Upload new image
            String imageUrl = fileUploadService.uploadProfileImage(profileImage, userId);
            user.setProfileImageUrl(imageUrl);
        }

        user.setUpdatedAt(LocalDateTime.now());
        return userRepository.save(user);
    }

    public List<String> generateUsernamesSuggestions(String baseName) {
        if (baseName == null || baseName.trim().isEmpty()) {
            throw new IllegalArgumentException("Base name is required for username generation");
        }

        String cleanBaseName = baseName.trim().toLowerCase()
            .replaceAll("[^a-z0-9]", "") // Remove special characters
            .replaceAll("\\s+", ""); // Remove spaces

        if (cleanBaseName.isEmpty()) {
            cleanBaseName = "user";
        }

        List<String> suggestions = new ArrayList<>();

        // Try base name first
        if (!userRepository.existsByUsername(cleanBaseName)) {
            suggestions.add(cleanBaseName);
        }

        // Add numbered variations
        for (int i = 1; suggestions.size() < 5; i++) {
            String candidate = cleanBaseName + i;
            if (!userRepository.existsByUsername(candidate)) {
                suggestions.add(candidate);
            }
        }

        // Add random number variations if still need more
        while (suggestions.size() < 5) {
            int randomNum = random.nextInt(9999) + 1;
            String candidate = cleanBaseName + randomNum;
            if (!userRepository.existsByUsername(candidate) && !suggestions.contains(candidate)) {
                suggestions.add(candidate);
            }
        }

        // Add some creative variations
        String[] suffixes = {"_dev", "_pro", "_user", "_x", "_official"};
        for (String suffix : suffixes) {
            if (suggestions.size() >= 5) break;
            String candidate = cleanBaseName + suffix;
            if (!userRepository.existsByUsername(candidate) && !suggestions.contains(candidate)) {
                suggestions.add(candidate);
            }
        }

        return suggestions.subList(0, Math.min(5, suggestions.size()));
    }

    public boolean isUsernameAvailable(String username) {
        if (username == null || username.trim().isEmpty()) {
            return false;
        }
        return !userRepository.existsByUsername(username.trim());
    }
}