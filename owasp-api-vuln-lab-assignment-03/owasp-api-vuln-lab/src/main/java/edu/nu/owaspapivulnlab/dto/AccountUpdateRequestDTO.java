package edu.nu.owaspapivulnlab.dto;

import jakarta.validation.constraints.NotNull;

// SECURITY FIX (Task 6): DTO for account updates
// Only allows balance updates
// Prevents ownerUserId, IBAN modifications
// For future PUT /api/accounts/{id} endpoint
public class AccountUpdateRequestDTO {

    @NotNull(message = "Balance is required")
    private Double balance;

    // SECURITY: Intentionally LIMITED fields
    // ownerUserId cannot be changed (prevents account takeover)
    // IBAN cannot be changed (prevents account redirects)

    public AccountUpdateRequestDTO() {}

    public AccountUpdateRequestDTO(Double balance) {
        this.balance = balance;
    }

    public Double getBalance() {
        return balance;
    }

    public void setBalance(Double balance) {
        this.balance = balance;
    }
}