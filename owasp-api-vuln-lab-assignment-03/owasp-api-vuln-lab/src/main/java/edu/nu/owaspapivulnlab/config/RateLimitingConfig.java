package edu.nu.owaspapivulnlab.config;

import io.github.resilience4j.ratelimiter.RateLimiter;
import io.github.resilience4j.ratelimiter.RateLimiterConfig;
import io.github.resilience4j.ratelimiter.RateLimiterRegistry;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.time.Duration;

@Configuration
public class RateLimitingConfig {

    // SECURITY FIX (Task 5): Configure rate limiters for different endpoints
    // Prevents brute force attacks, DoS, and resource exhaustion

    /**
     * Login Rate Limiter: 5 attempts per 15 minutes
     * Purpose: Prevent brute force attacks on login
     */
    @Bean
    public RateLimiter loginLimiter() {
        RateLimiterConfig config = RateLimiterConfig.custom()
                .limitRefreshPeriod(Duration.ofMinutes(15))  // Reset every 15 minutes
                .limitForPeriod(5)                            // Max 5 requests per period
                .timeoutDuration(Duration.ofSeconds(1))      // Wait 1 second for permission
                .build();

        RateLimiterRegistry registry = RateLimiterRegistry.of(config);
        return registry.rateLimiter("loginLimiter", config);
    }

    /**
     * Signup Rate Limiter: 10 signups per hour
     * Purpose: Prevent account creation spam
     */
    @Bean
    public RateLimiter signupLimiter() {
        RateLimiterConfig config = RateLimiterConfig.custom()
                .limitRefreshPeriod(Duration.ofHours(1))    // Reset every hour
                .limitForPeriod(10)                          // Max 10 requests per hour
                .timeoutDuration(Duration.ofSeconds(1))
                .build();

        RateLimiterRegistry registry = RateLimiterRegistry.of(config);
        return registry.rateLimiter("signupLimiter", config);
    }

    /**
     * Transfer Rate Limiter: 10 transfers per minute
     * Purpose: Prevent fraud and resource exhaustion
     */
    @Bean
    public RateLimiter transferLimiter() {
        RateLimiterConfig config = RateLimiterConfig.custom()
                .limitRefreshPeriod(Duration.ofMinutes(1))   // Reset every minute
                .limitForPeriod(10)                          // Max 10 transfers per minute
                .timeoutDuration(Duration.ofSeconds(1))
                .build();

        RateLimiterRegistry registry = RateLimiterRegistry.of(config);
        return registry.rateLimiter("transferLimiter", config);
    }

    /**
     * Get User Rate Limiter: 30 requests per minute
     * Purpose: Prevent user enumeration attacks
     */
    @Bean
    public RateLimiter getUserLimiter() {
        RateLimiterConfig config = RateLimiterConfig.custom()
                .limitRefreshPeriod(Duration.ofMinutes(1))   // Reset every minute
                .limitForPeriod(30)                          // Max 30 requests per minute
                .timeoutDuration(Duration.ofSeconds(1))
                .build();

        RateLimiterRegistry registry = RateLimiterRegistry.of(config);
        return registry.rateLimiter("getUserLimiter", config);
    }

    /**
     * Get Balance Rate Limiter: 20 requests per minute
     * Purpose: Prevent information gathering attacks
     */
    @Bean
    public RateLimiter getBalanceLimiter() {
        RateLimiterConfig config = RateLimiterConfig.custom()
                .limitRefreshPeriod(Duration.ofMinutes(1))   // Reset every minute
                .limitForPeriod(20)                          // Max 20 requests per minute
                .timeoutDuration(Duration.ofSeconds(1))
                .build();

        RateLimiterRegistry registry = RateLimiterRegistry.of(config);
        return registry.rateLimiter("getBalanceLimiter", config);
    }

    /**
     * General API Rate Limiter: 60 requests per minute
     * Purpose: General protection for all API endpoints
     */
    @Bean
    public RateLimiter generalLimiter() {
        RateLimiterConfig config = RateLimiterConfig.custom()
                .limitRefreshPeriod(Duration.ofMinutes(1))   // Reset every minute
                .limitForPeriod(60)                          // Max 60 requests per minute
                .timeoutDuration(Duration.ofSeconds(1))
                .build();

        RateLimiterRegistry registry = RateLimiterRegistry.of(config);
        return registry.rateLimiter("generalLimiter", config);
    }
}