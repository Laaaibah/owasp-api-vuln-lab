package edu.nu.owaspapivulnlab.config;

import edu.nu.owaspapivulnlab.model.AppUser;
import edu.nu.owaspapivulnlab.repo.AppUserRepository;
import jakarta.annotation.PostConstruct;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.crypto.password.PasswordEncoder;

@Configuration
public class DataSeeder {

    @Autowired
    private AppUserRepository userRepo;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @PostConstruct
    public void seedData() {
        if (userRepo.count() == 0) {
            AppUser u1 = new AppUser();
            u1.setUsername("alice");
            u1.setEmail("alice@example.com");
            u1.setRole("USER");
            u1.setAdmin(false);
            u1.setPassword(passwordEncoder.encode("alice123"));

            AppUser u2 = new AppUser();
            u2.setUsername("admin");
            u2.setEmail("admin@example.com");
            u2.setRole("ADMIN");
            u2.setAdmin(true);
            u2.setPassword(passwordEncoder.encode("admin123"));

            userRepo.save(u1);
            userRepo.save(u2);
        }
    }
}