package edu.nu.owaspapivulnlab.web;

import io.github.resilience4j.ratelimiter.annotation.RateLimiter;
import edu.nu.owaspapivulnlab.dto.UserCreateRequestDTO;
import edu.nu.owaspapivulnlab.dto.UserResponseDTO;
import edu.nu.owaspapivulnlab.model.AppUser;
import edu.nu.owaspapivulnlab.repo.AppUserRepository;
import edu.nu.owaspapivulnlab.service.JwtService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

@RestController
@RequestMapping("/api/auth")
public class AuthController {

    @Autowired
    private AppUserRepository userRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Autowired
    private JwtService jwtService;

    @PostMapping("/signup")
    @RateLimiter(name = "signupLimiter", fallbackMethod = "signupFallback")
    public ResponseEntity<?> signup(@RequestBody UserCreateRequestDTO userDTO) {
        if (userRepository.findByUsername(userDTO.getUsername()).isPresent()) {
            return ResponseEntity.status(HttpStatus.CONFLICT).body("Username already exists");
        }
        
        AppUser newUser = new AppUser();
        newUser.setUsername(userDTO.getUsername());
        newUser.setEmail(userDTO.getEmail());
        newUser.setPassword(passwordEncoder.encode(userDTO.getPassword()));
        newUser.setRole("USER");
        newUser.setAdmin(false);

        AppUser savedUser = userRepository.save(newUser);

        UserResponseDTO response = new UserResponseDTO(
            savedUser.getId(),
            savedUser.getUsername(),
            savedUser.getEmail()
        );

        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @PostMapping("/login")
    @RateLimiter(name = "loginLimiter", fallbackMethod = "loginFallback")
    public ResponseEntity<?> login(@RequestBody Map<String, String> loginData) {
        String username = loginData.get("username");
        String password = loginData.get("password");

        // =================================================================
        // ✅ SECURITY FIX (Task 9): Validate that inputs are not blank.
        // Before: An empty username/password would hit the database.
        // After: Rejects the request immediately with a 400 Bad Request.
        // =================================================================
        if (username == null || username.isBlank() || password == null || password.isBlank()) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                .body(Map.of("error", "Bad Request", "message", "Username and password must not be empty."));
        }

        AppUser user = userRepository.findByUsername(username)
                .orElseThrow(() -> new RuntimeException("User not found"));
        
        if (!passwordEncoder.matches(password, user.getPassword())) {
            throw new RuntimeException("Invalid credentials");
        }

        Map<String, Object> claims = new HashMap<>();
        claims.put("userId", user.getId());
        claims.put("role", user.getRole());
        claims.put("isAdmin", user.isAdmin());

        String token = jwtService.issue(user.getUsername(), claims);
        return ResponseEntity.ok(Map.of("token", token));
    }

    // Fallback Methods
    public ResponseEntity<?> signupFallback(UserCreateRequestDTO userDTO, Exception e) {
        return ResponseEntity.status(HttpStatus.TOO_MANY_REQUESTS)
            .body(Map.of("error", "Too many signup attempts."));
    }

    public ResponseEntity<?> loginFallback(Map<String, String> loginData, Exception e) {
        return ResponseEntity.status(HttpStatus.TOO_MANY_REQUESTS)
            .body(Map.of("error", "Too many login attempts."));
    }
}