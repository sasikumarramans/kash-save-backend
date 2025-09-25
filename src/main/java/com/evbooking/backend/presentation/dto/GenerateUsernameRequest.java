package com.evbooking.backend.presentation.dto;

import jakarta.validation.constraints.Size;

public class GenerateUsernameRequest {
    @Size(min = 2, max = 50, message = "Base name must be between 2 and 50 characters")
    private String baseName;

    public GenerateUsernameRequest() {}

    public GenerateUsernameRequest(String baseName) {
        this.baseName = baseName;
    }

    public String getBaseName() { return baseName; }
    public void setBaseName(String baseName) { this.baseName = baseName; }
}