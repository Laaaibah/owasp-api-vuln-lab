package edu.nu.owaspapivulnlab.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

import org.springframework.beans.factory.annotation.Autowired; // ADDED
import edu.nu.owaspapivulnlab.config.JwtFilter; // ADDED

@Configuration
@EnableWebSecurity
@EnableMethodSecurity(prePostEnabled = true)
public class SecurityConfig {

    @Autowired // ADDED
    private JwtFilter jwtFilter; // ADDED

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
            // Disable CSRF for API testing
            .csrf(csrf -> csrf.disable())
            
            // Allow H2 console
            .headers(headers -> headers
                .frameOptions(frame -> frame.sameOrigin()))
            
            .authorizeHttpRequests(auth -> auth
                // Public endpoints (H2 console)
                .requestMatchers("/h2-console/**").permitAll()
                
                // Public auth endpoints
                .requestMatchers("/api/auth/**").permitAll()
                
                // Admin endpoints
                .requestMatchers("/api/admin/**").hasRole("ADMIN")
                
                // Protected endpoints
                .requestMatchers("/api/users/**").authenticated()
                .requestMatchers("/api/accounts/**").authenticated()
                
                // Everything else protected
                .anyRequest().authenticated()
            )
            
            // Register JWT filter before UsernamePasswordAuthenticationFilter
            .addFilterBefore(jwtFilter, UsernamePasswordAuthenticationFilter.class) // ADDED
            
            // Enable HTTP Basic Auth (important for Postman)
            .httpBasic(Customizer.withDefaults())
            
            // Enable form login (optional, for browser access)
            .formLogin(Customizer.withDefaults());

        return http.build();
    }

    @Bean
    public AuthenticationManager authenticationManager(AuthenticationConfiguration authConfig) throws Exception {
        return authConfig.getAuthenticationManager();
    }
}
