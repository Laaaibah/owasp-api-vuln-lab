
package edu.nu.owaspapivulnlab.web;

import io.github.resilience4j.ratelimiter.annotation.RateLimiter;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;
import edu.nu.owaspapivulnlab.dto.UserCreateRequestDTO;
import edu.nu.owaspapivulnlab.dto.UserResponseDTO;
import edu.nu.owaspapivulnlab.exception.AccessDeniedException;
import edu.nu.owaspapivulnlab.exception.ResourceNotFoundException;
import edu.nu.owaspapivulnlab.model.AppUser;
import edu.nu.owaspapivulnlab.repo.AppUserRepository;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/users")
public class UserController {
    private final AppUserRepository users;
    private final PasswordEncoder passwordEncoder;

    public UserController(AppUserRepository users, PasswordEncoder passwordEncoder) {
        this.users = users;
        this.passwordEncoder = passwordEncoder;
    }

    // SECURITY FIX (Task 5): Rate limit user profile access to 30 per minute
    // Before: Unlimited user lookups (enumeration attacks)
    // After: Max 30 per minute (prevents user discovery)
    @GetMapping("/{id}")
    @PreAuthorize("authenticated")
    @RateLimiter(name = "getUserLimiter", fallbackMethod = "getUserFallback")
    public UserResponseDTO get(@PathVariable Long id, Authentication auth) {
        AppUser currentUser = users.findByUsername(auth.getName())
            .orElseThrow(() -> new ResourceNotFoundException("Current user not found"));
        
        AppUser requestedUser = users.findById(id)
            .orElseThrow(() -> new ResourceNotFoundException("User not found"));
        
        if (!currentUser.isAdmin() && !currentUser.getId().equals(requestedUser.getId())) {
            throw new AccessDeniedException(
                "You do not have permission to access this user's profile"
            );
        }
        
        return new UserResponseDTO(
            requestedUser.getId(),
            requestedUser.getUsername(),
            requestedUser.getEmail()
        );
    }

    @PostMapping
    public ResponseEntity<UserResponseDTO> create(@Valid @RequestBody UserCreateRequestDTO body) {
        AppUser user = new AppUser();
        user.setUsername(body.getUsername());
        user.setEmail(body.getEmail());
        user.setPassword(passwordEncoder.encode(body.getPassword()));
        user.setRole("USER");
        user.setAdmin(false);
        
        AppUser savedUser = users.save(user);
        
        UserResponseDTO response = new UserResponseDTO(
            savedUser.getId(),
            savedUser.getUsername(),
            savedUser.getEmail()
        );
        
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @GetMapping("/search")
    @PreAuthorize("authenticated")
    public List<UserResponseDTO> search(@RequestParam String q) {
        return users.search(q).stream()
            .map(user -> new UserResponseDTO(user.getId(), user.getUsername(), user.getEmail()))
            .collect(Collectors.toList());
    }

    @GetMapping
    @PreAuthorize("authenticated")
    public List<UserResponseDTO> list() {
        return users.findAll().stream()
            .map(user -> new UserResponseDTO(user.getId(), user.getUsername(), user.getEmail()))
            .collect(Collectors.toList());
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<?> delete(@PathVariable Long id) {
        users.findById(id)
            .orElseThrow(() -> new ResourceNotFoundException("User not found"));
        
        users.deleteById(id);
        
        Map<String, String> response = new HashMap<>();
        response.put("status", "deleted");
        return ResponseEntity.ok(response);
    }

    // Fallback for user lookup rate limiting
    public ResponseEntity<?> getUserFallback(Long id, Authentication auth, Exception e) {
        return ResponseEntity.status(429)
            .body(Map.of("error", "Too many user lookups. Please try again later."));
    }
}