package edu.nu.owaspapivulnlab.web;

import io.github.resilience4j.ratelimiter.annotation.RateLimiter;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;
import edu.nu.owaspapivulnlab.dto.AccountResponseDTO;
import edu.nu.owaspapivulnlab.exception.AccessDeniedException;
import edu.nu.owaspapivulnlab.exception.ResourceNotFoundException;
import edu.nu.owaspapivulnlab.model.Account;
import edu.nu.owaspapivulnlab.model.AppUser;
import edu.nu.owaspapivulnlab.repo.AccountRepository;
import edu.nu.owaspapivulnlab.repo.AppUserRepository;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/accounts")
public class AccountController {

    private final AccountRepository accounts;
    private final AppUserRepository users;

    public AccountController(AccountRepository accounts, AppUserRepository users) {
        this.accounts = accounts;
        this.users = users;
    }

    private void validateAccountOwnership(Account account, AppUser user) {
        if (!account.getOwnerUserId().equals(user.getId()) && !user.isAdmin()) {
            throw new AccessDeniedException("You do not have permission to access this account.");
        }
    }

    @GetMapping("/{id}/balance")
    @RateLimiter(name = "getBalanceLimiter", fallbackMethod = "balanceFallback")
    @PreAuthorize("authenticated")
    public ResponseEntity<?> balance(@PathVariable Long id, Authentication auth) {
        AppUser currentUser = users.findByUsername(auth.getName())
            .orElseThrow(() -> new ResourceNotFoundException("Current user not found"));

        Account account = accounts.findById(id)
            .orElseThrow(() -> new ResourceNotFoundException("Account not found"));

        validateAccountOwnership(account, currentUser);

        Map<String, Double> response = new HashMap<>();
        response.put("balance", account.getBalance());
        return ResponseEntity.ok(response);
    }

    @PostMapping("/{id}/transfer")
    // @RateLimiter(name = "transferLimiter", fallbackMethod = "transferFallback") // Disabled for testing
    @PreAuthorize("authenticated")
    public ResponseEntity<?> transfer(
            @PathVariable Long id,
            @RequestParam Double amount,
            Authentication auth) {

        // =================================================================
        // SECURITY FIX (Task 9): Input Validation
        // This block rejects any transfer amount that is not a positive number.
        // =================================================================
        if (amount == null || amount <= 0) {
            throw new IllegalArgumentException("Transfer amount must be a positive number.");
        }

        AppUser currentUser = users.findByUsername(auth.getName())
                .orElseThrow(() -> new ResourceNotFoundException("Current user not found"));

        Account account = accounts.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Account not found"));

        validateAccountOwnership(account, currentUser);

        // Also validate that the user has enough money
        if (account.getBalance() < amount) {
            throw new IllegalArgumentException("Insufficient funds for this transfer.");
        }

        account.setBalance(account.getBalance() - amount);
        accounts.save(account);

        Map<String, Object> response = new HashMap<>();
        response.put("status", "ok");
        response.put("remaining", account.getBalance());
        return ResponseEntity.ok(response);
    }

    @GetMapping("/mine")
    @PreAuthorize("authenticated")
    public List<AccountResponseDTO> mine(Authentication auth) {
        AppUser me = users.findByUsername(auth.getName())
            .orElseThrow(() -> new ResourceNotFoundException("User not found"));
        
        return accounts.findByOwnerUserId(me.getId()).stream()
            .map(account -> new AccountResponseDTO(
                account.getId(),
                account.getIban(),
                account.getBalance()
            ))
            .collect(Collectors.toList());
    }

    // Fallback methods for rate limiters
    public ResponseEntity<?> balanceFallback(Long id, Authentication auth, Exception e) {
        return ResponseEntity.status(429).body(Map.of("error", "Too many balance inquiries."));
    }

    public ResponseEntity<?> transferFallback(Long id, Double amount, Authentication auth, Exception e) {
        return ResponseEntity.status(429).body(Map.of("error", "Too many transfer attempts."));
    }
}