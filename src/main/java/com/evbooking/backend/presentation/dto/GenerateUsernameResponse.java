package com.evbooking.backend.presentation.dto;

import java.util.List;

public class GenerateUsernameResponse {
    private List<String> suggestions;
    private String message;

    public GenerateUsernameResponse() {}

    public GenerateUsernameResponse(List<String> suggestions, String message) {
        this.suggestions = suggestions;
        this.message = message;
    }

    public List<String> getSuggestions() { return suggestions; }
    public void setSuggestions(List<String> suggestions) { this.suggestions = suggestions; }

    public String getMessage() { return message; }
    public void setMessage(String message) { this.message = message; }
}