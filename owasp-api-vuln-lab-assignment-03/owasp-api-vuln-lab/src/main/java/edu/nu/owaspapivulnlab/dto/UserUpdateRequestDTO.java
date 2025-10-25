package edu.nu.owaspapivulnlab.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.Size;

// SECURITY FIX (Task 6): DTO for user updates (PUT/PATCH)
// Only allows email updates
// Prevents password, role, isAdmin modifications
// For future PUT /api/users/{id} endpoint
public class UserUpdateRequestDTO {

    @Email(message = "Email must be valid format")
    private String email;

    // SECURITY: Intentionally LIMITED fields
    // Password changes require separate endpoint
    // Role/isAdmin cannot be changed by users (admin only if needed)

    public UserUpdateRequestDTO() {}

    public UserUpdateRequestDTO(String email) {
        this.email = email;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }
}