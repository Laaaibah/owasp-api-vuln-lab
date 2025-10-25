package edu.nu.owaspapivulnlab.config;

import edu.nu.owaspapivulnlab.model.AppUser;
import edu.nu.owaspapivulnlab.repo.AppUserRepository;
import edu.nu.owaspapivulnlab.service.JwtService;
import io.jsonwebtoken.Claims;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;

@Component
public class JwtFilter extends OncePerRequestFilter {

    @Autowired
    private JwtService jwtService;

    @Autowired
    private AppUserRepository userRepository;

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
            throws ServletException, IOException {
        final String token = extractTokenFromHeader(request);

        if (token != null) {
            try {
                // 1. Validate the token
                Claims claims = jwtService.validateToken(token);
                String username = claims.getSubject();

                // 2. If valid, load user and their roles (authorities)
                if (username != null && SecurityContextHolder.getContext().getAuthentication() == null) {
                    AppUser user = userRepository.findByUsername(username).orElse(null);

                    if (user != null) {
                        // =================== THE CRITICAL FIX ===================
                        // SECURITY FIX: Create a list of authorities from the user's roles.
                        // Without this, @PreAuthorize("hasRole('ADMIN')") will always fail.
                        Collection<GrantedAuthority> authorities = new ArrayList<>();
                        if (user.isAdmin()) {
                            authorities.add(new SimpleGrantedAuthority("ROLE_ADMIN"));
                        } else {
                            authorities.add(new SimpleGrantedAuthority("ROLE_USER"));
                        }
                        // =========================================================

                        // 3. Create an authentication token with the user's details AND authorities
                        UsernamePasswordAuthenticationToken authToken = new UsernamePasswordAuthenticationToken(
                                user.getUsername(),
                                null,
                                authorities // <-- Pass the authorities here!
                        );

                        authToken.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));

                        // 4. Set the authentication object in the security context
                        SecurityContextHolder.getContext().setAuthentication(authToken);
                    }
                }
            } catch (Exception e) {
                // Log the exception but allow the request to continue.
                // Endpoint-level security will deny access if authentication is missing.
                logger.debug("JWT validation failed: " + e.getMessage());
            }
        }

        filterChain.doFilter(request, response);
    }

    private String extractTokenFromHeader(HttpServletRequest request) {
        String authHeader = request.getHeader("Authorization");
        if (authHeader != null && authHeader.startsWith("Bearer ")) {
            return authHeader.substring(7);
        }
        return null;
    }
}