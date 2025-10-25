package edu.nu.owaspapivulnlab.config;

import edu.nu.owaspapivulnlab.model.AppUser;
import edu.nu.owaspapivulnlab.repo.AppUserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.Collection;

@Service
public class CustomUserDetailsService implements UserDetailsService {

    @Autowired
    private AppUserRepository userRepository;

    // SECURITY FIX: This is CRITICAL for Spring Security to authenticate users
    // Spring Security needs UserDetailsService to load user info during authentication
    // Without this, Basic Auth cannot work
    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        // Find user in database
        AppUser appUser = userRepository.findByUsername(username)
            .orElseThrow(() -> new UsernameNotFoundException("User not found: " + username));

        // Build authorities/roles
        Collection<GrantedAuthority> authorities = new ArrayList<>();
        
        // Add ADMIN role if user is admin
        if (appUser.isAdmin()) {
            authorities.add(new SimpleGrantedAuthority("ROLE_ADMIN"));
        } else {
            // Add USER role for regular users
            authorities.add(new SimpleGrantedAuthority("ROLE_USER"));
        }

        // Return Spring Security User object
        // This is what Spring Security uses for authentication
        return new User(
            appUser.getUsername(),
            appUser.getPassword(),  // This is the BCrypt hashed password
            authorities
        );
    }
}