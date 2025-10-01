package com.evbooking.backend.presentation.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public class CreateBookRequest {

    @NotBlank(message = "Book name is required")
    @Size(max = 100, message = "Book name must not exceed 100 characters")
    private String name;

    @Size(max = 500, message = "Description must not exceed 500 characters")
    private String description;

    @Size(max = 3, message = "Currency must be a valid 3-letter code")
    private String currency;

    public CreateBookRequest() {}

    public CreateBookRequest(String name, String description, String currency) {
        this.name = name;
        this.description = description;
        this.currency = currency;
    }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }

    public String getCurrency() { return currency; }
    public void setCurrency(String currency) { this.currency = currency; }
}