package com.evbooking.backend.presentation.dto.split;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.Size;

import java.util.List;

public class CreateGroupRequest {
    @NotBlank(message = "Group name is required")
    @Size(max = 255, message = "Group name cannot exceed 255 characters")
    private String name;

    @Size(max = 1000, message = "Description cannot exceed 1000 characters")
    private String description;

    private String currency = "INR";

    @NotEmpty(message = "At least one member username is required")
    private List<String> memberUsernames;

    public CreateGroupRequest() {}

    public CreateGroupRequest(String name, String description, String currency, List<String> memberUsernames) {
        this.name = name;
        this.description = description;
        this.currency = currency;
        this.memberUsernames = memberUsernames;
    }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }

    public String getCurrency() { return currency; }
    public void setCurrency(String currency) { this.currency = currency; }

    public List<String> getMemberUsernames() { return memberUsernames; }
    public void setMemberUsernames(List<String> memberUsernames) { this.memberUsernames = memberUsernames; }
}