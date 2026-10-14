package com.delicious.common.security;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Service;

import javax.crypto.SecretKey;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.function.Function;

/**
 * Service responsible for all JWT operations:
 * - Token generation (signing with HS256)
 * - Token validation
 * - Claim extraction (email, userId, expiration)
 */
@Service
public class JwtService {

    @Value("${jwt.secret}")
    private String secretKey;

    @Value("${jwt.expiration-ms:900000}")
    private long expirationMs;

    @Value("${jwt.refresh-expiration-ms:604800000}")
    private long refreshExpirationMs;

    /**
     * Generate a signed JWT access token for the given UserDetails.
     * Stores userId, role, and tokenType as extra claims.
     */
    public String generateToken(UserDetails userDetails, Long userId) {
        Map<String, Object> extraClaims = new HashMap<>();
        extraClaims.put("userId", userId);
        extraClaims.put("tokenType", "ACCESS");
        if (!userDetails.getAuthorities().isEmpty()) {
            extraClaims.put("role", userDetails.getAuthorities().iterator().next().getAuthority());
        }
        return buildToken(extraClaims, userDetails, expirationMs);
    }

    /**
     * Generate a signed JWT refresh token for the given UserDetails.
     * Stores userId and tokenType="REFRESH".
     */
    public String generateRefreshToken(UserDetails userDetails, Long userId) {
        Map<String, Object> extraClaims = new HashMap<>();
        extraClaims.put("userId", userId);
        extraClaims.put("tokenType", "REFRESH");
        return buildToken(extraClaims, userDetails, refreshExpirationMs);
    }

    private String buildToken(Map<String, Object> extraClaims, UserDetails userDetails, long expirationDurationMs) {
        return Jwts.builder()
                .claims(extraClaims)
                .subject(userDetails.getUsername())  // email
                .issuedAt(new Date(System.currentTimeMillis()))
                .expiration(new Date(System.currentTimeMillis() + expirationDurationMs))
                .signWith(getSigningKey())
                .compact();
    }

    /**
     * Validates a JWT token against the provided UserDetails.
     * Checks: signature, subject (email) match, and expiration.
     */
    public boolean isTokenValid(String token, UserDetails userDetails) {
        final String email = extractEmail(token);
        return email != null && email.equals(userDetails.getUsername()) && !isTokenExpired(token);
    }

    /**
     * Extract token type claim ("ACCESS" or "REFRESH").
     */
    public String extractTokenType(String token) {
        return extractClaim(token, claims -> claims.get("tokenType", String.class));
    }

    /**
     * Checks if the token is marked as an ACCESS token.
     * Tokens without explicit tokenType claim default to ACCESS for backward compatibility.
     */
    public boolean isAccessToken(String token) {
        String type = extractTokenType(token);
        return type == null || "ACCESS".equalsIgnoreCase(type);
    }

    /**
     * Checks if the token is specifically a REFRESH token.
     */
    public boolean isRefreshToken(String token) {
        String type = extractTokenType(token);
        return "REFRESH".equalsIgnoreCase(type);
    }

    /** Extract the email (subject) from the JWT. */
    public String extractEmail(String token) {
        return extractClaim(token, Claims::getSubject);
    }

    /** Extract the userId stored as a custom claim. */
    public Long extractUserId(String token) {
        Object userId = extractClaim(token, claims -> claims.get("userId"));
        if (userId instanceof Integer) {
            return ((Integer) userId).longValue();
        }
        if (userId instanceof Long) {
            return (Long) userId;
        }
        return null;
    }

    /** Extract the role stored as a custom claim. */
    public String extractRole(String token) {
        return extractClaim(token, claims -> claims.get("role", String.class));
    }

    private boolean isTokenExpired(String token) {
        return extractExpiration(token).before(new Date());
    }

    private Date extractExpiration(String token) {
        return extractClaim(token, Claims::getExpiration);
    }

    public <T> T extractClaim(String token, Function<Claims, T> claimsResolver) {
        final Claims claims = extractAllClaims(token);
        return claimsResolver.apply(claims);
    }

    private Claims extractAllClaims(String token) {
        return Jwts.parser()
                .verifyWith(getSigningKey())
                .build()
                .parseSignedClaims(token)
                .getPayload();
    }

    private SecretKey getSigningKey() {
        byte[] keyBytes = Decoders.BASE64.decode(secretKey);
        return Keys.hmacShaKeyFor(keyBytes);
    }
}
