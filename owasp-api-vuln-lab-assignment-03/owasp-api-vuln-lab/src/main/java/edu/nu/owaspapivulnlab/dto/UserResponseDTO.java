
package edu.nu.owaspapivulnlab.dto;

// SECURITY FIX (Task 4): DTO for user responses
// Only returns safe fields: id, username, email
// Hides internal fields: password, role, isAdmin
// Prevents data exposure
public class UserResponseDTO {

    private Long id;
    private String username;
    private String email;

    // Constructors
    public UserResponseDTO() {}

    public UserResponseDTO(Long id, String username, String email) {
        this.id = id;
        this.username = username;
        this.email = email;
    }

    // Factory method: Convert AppUser entity to DTO
    public static UserResponseDTO fromAppUser(Object appUser) {
        try {
            // Use reflection to avoid circular dependency
            Long id = (Long) appUser.getClass().getMethod("getId").invoke(appUser);
            String username = (String) appUser.getClass().getMethod("getUsername").invoke(appUser);
            String email = (String) appUser.getClass().getMethod("getEmail").invoke(appUser);
            return new UserResponseDTO(id, username, email);
        } catch (Exception e) {
            throw new RuntimeException("Failed to convert AppUser to DTO", e);
        }
    }

    // Getters and Setters
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }
}