package edu.nu.owaspapivulnlab.dto;

import jakarta.validation.constraints.NotBlank;

// SECURITY FIX (Task 6): DTO for account creation
// Only accepts IBAN from client
// Server sets ownerUserId, balance, and other defaults
// For future POST /api/accounts endpoint
public class AccountCreateRequestDTO {

    @NotBlank(message = "IBAN is required")
    private String iban;

    // SECURITY: Intentionally NO ownerUserId
    // SECURITY: Intentionally NO balance
    // Server sets these based on authenticated user

    public AccountCreateRequestDTO() {}

    public AccountCreateRequestDTO(String iban) {
        this.iban = iban;
    }

    public String getIban() {
        return iban;
    }

    public void setIban(String iban) {
        this.iban = iban;
    }
}