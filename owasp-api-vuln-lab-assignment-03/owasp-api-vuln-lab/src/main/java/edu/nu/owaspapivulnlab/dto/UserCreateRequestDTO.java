package edu.nu.owaspapivulnlab.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

// SECURITY FIX (Task 6): Enhanced DTO with validation
// Prevents mass assignment of sensitive fields
// Only accepts: username, password, email
// Rejects: role, isAdmin, and any other fields
public class UserCreateRequestDTO {

    @NotBlank(message = "Username is required")
    @Size(min = 3, max = 50, message = "Username must be 3-50 characters")
    private String username;

    @NotBlank(message = "Password is required")
    @Size(min = 6, max = 100, message = "Password must be 6-100 characters")
    private String password;

    @NotBlank(message = "Email is required")
    @Email(message = "Email must be valid format")
    private String email;

    // SECURITY: Intentionally NO role field
    // SECURITY: Intentionally NO isAdmin field
    // These are set server-side with defaults

    public UserCreateRequestDTO() {}

    public UserCreateRequestDTO(String username, String password, String email) {
        this.username = username;
        this.password = password;
        this.email = email;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }
}
