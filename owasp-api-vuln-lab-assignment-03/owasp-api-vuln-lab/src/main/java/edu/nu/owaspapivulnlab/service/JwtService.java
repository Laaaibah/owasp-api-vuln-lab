package edu.nu.owaspapivulnlab.service;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.util.Date;
import java.util.Map;
import java.util.UUID;

@Service
public class JwtService {

    @Value("${jwt.secret:#{null}}")
    private String secret;

    @Value("${jwt.issuer:secure-api}")
    private String issuer;

    @Value("${jwt.audience:api-users}")
    private String audience;

    @Value("${jwt.expiration:900}")
    private long expirationSeconds;

    private SecretKey key;

    // This method initializes the signing key.
    // It's called lazily the first time a key is needed.
    private SecretKey getSigningKey() {
        if (key != null) {
            return key;
        }

        // Use the secret from application.properties.
        // In a real production app, this should be an environment variable.
        String secretStr = this.secret;
        if (secretStr == null || secretStr.isEmpty() || "default-secure-secret-key-for-testing-please-change".equals(secretStr)) {
            // SECURITY WARNING: This is for development only.
            // A strong, configured secret is required for production.
            System.err.println("******************************************************************");
            System.err.println("WARNING: JWT secret is not set. Using a temporary, insecure key.");
            System.err.println("SET the 'jwt.secret' property or JWT_SECRET_KEY env variable.");
            System.err.println("******************************************************************");
            // Generate a secure, random key for development.
            key = Jwts.SIG.HS256.key().build();
            return key;
        }

        // Create the key from the configured secret string.
        byte[] keyBytes = secretStr.getBytes(StandardCharsets.UTF_8);
        if (keyBytes.length < 32) {
            // Enforce key strength for HS256.
            throw new IllegalArgumentException("JWT secret key must be at least 32 bytes (256 bits) long for HS256 algorithm.");
        }
        key = Keys.hmacShaKeyFor(keyBytes);
        return key;
    }

    /**
     * Issues a new JWT for a given user.
     */
    public String issue(String subject, Map<String, Object> claims) {
        Date now = new Date();
        Date expiryDate = new Date(now.getTime() + expirationSeconds * 1000);

        return Jwts.builder()
                .claims(claims)
                .subject(subject)
                .issuer(issuer)
                .audience()
                    .add(audience)
                    .and()
                .issuedAt(now)
                .expiration(expiryDate)
                .id(UUID.randomUUID().toString())
                .signWith(getSigningKey()) // The key itself determines the algorithm.
                .compact();
    }

    /**
     * Validates a token and returns its claims.
     */
    public Claims validateToken(String token) {
        try {
            // The modern, correct way to parse and validate tokens.
            return Jwts.parser() // The new parser() is a shortcut for parserBuilder()
                    .verifyWith(getSigningKey())
                    .requireIssuer(issuer)
                    .requireAudience(audience)
                    .build()
                    .parseSignedClaims(token)
                    .getPayload();
        } catch (ExpiredJwtException e) {
            throw new RuntimeException("JWT token has expired", e);
        } catch (JwtException | IllegalArgumentException e) {
            // Catches all other JWT-related errors (bad signature, malformed, etc.)
            throw new RuntimeException("Invalid JWT token: " + e.getMessage(), e);
        }
    }

    public String getSubject(String token) {
        return validateToken(token).getSubject();
    }
}