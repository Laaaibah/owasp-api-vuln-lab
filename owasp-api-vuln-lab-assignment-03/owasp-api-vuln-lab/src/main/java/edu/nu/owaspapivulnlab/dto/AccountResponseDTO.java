
package edu.nu.owaspapivulnlab.dto;

// SECURITY FIX (Task 4): DTO for account responses
// Only returns safe fields: id, iban, balance
// Hides ownerUserId (client already knows it's their account)
// Prevents data exposure
public class AccountResponseDTO {

    private Long id;
    private String iban;
    private Double balance;

    // Constructors
    public AccountResponseDTO() {}

    public AccountResponseDTO(Long id, String iban, Double balance) {
        this.id = id;
        this.iban = iban;
        this.balance = balance;
    }

    // Factory method: Convert Account entity to DTO
    public static AccountResponseDTO fromAccount(Object account) {
        try {
            // Use reflection to avoid circular dependency
            Long id = (Long) account.getClass().getMethod("getId").invoke(account);
            String iban = (String) account.getClass().getMethod("getIban").invoke(account);
            Double balance = (Double) account.getClass().getMethod("getBalance").invoke(account);
            return new AccountResponseDTO(id, iban, balance);
        } catch (Exception e) {
            throw new RuntimeException("Failed to convert Account to DTO", e);
        }
    }

    // Getters and Setters
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getIban() {
        return iban;
    }

    public void setIban(String iban) {
        this.iban = iban;
    }

    public Double getBalance() {
        return balance;
    }

    public void setBalance(Double balance) {
        this.balance = balance;
    }
}