package com.delicious.common.security;

import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseCookie;
import org.springframework.stereotype.Service;

import java.util.Arrays;
import java.util.Optional;

/**
 * Service to manage HTTP cookies for authentication tokens across environments.
 * Supports environment-specific secure, sameSite, and domain settings.
 */
@Service
public class CookieService {

    public static final String ACCESS_TOKEN_COOKIE_NAME = "delicious_token";
    public static final String REFRESH_TOKEN_COOKIE_NAME = "delicious_refresh_token";
    public static final String USER_ROLE_COOKIE_NAME = "delicious_role";

    @Value("${cookie.secure:false}")
    private boolean secure;

    @Value("${cookie.same-site:Lax}")
    private String sameSite;

    @Value("${cookie.domain:}")
    private String domain;

    @Value("${jwt.expiration-ms:900000}")
    private long accessTokenExpirationMs;

    @Value("${jwt.refresh-expiration-ms:604800000}")
    private long refreshTokenExpirationMs;

    public ResponseCookie createAccessTokenCookie(String token) {
        return buildCookie(ACCESS_TOKEN_COOKIE_NAME, token, accessTokenExpirationMs / 1000, true);
    }

    public ResponseCookie createRefreshTokenCookie(String refreshToken) {
        return buildCookie(REFRESH_TOKEN_COOKIE_NAME, refreshToken, refreshTokenExpirationMs / 1000, true);
    }

    public ResponseCookie createUserRoleCookie(String role) {
        // Non-httpOnly so frontend client / middleware can inspect roles
        return buildCookie(USER_ROLE_COOKIE_NAME, role, refreshTokenExpirationMs / 1000, false);
    }

    public ResponseCookie createCleanAccessTokenCookie() {
        return buildCookie(ACCESS_TOKEN_COOKIE_NAME, "", 0, true);
    }

    public ResponseCookie createCleanRefreshTokenCookie() {
        return buildCookie(REFRESH_TOKEN_COOKIE_NAME, "", 0, true);
    }

    public ResponseCookie createCleanUserRoleCookie() {
        return buildCookie(USER_ROLE_COOKIE_NAME, "", 0, false);
    }

    private ResponseCookie buildCookie(String name, String value, long maxAgeSeconds, boolean httpOnly) {
        ResponseCookie.ResponseCookieBuilder builder = ResponseCookie.from(name, value)
                .httpOnly(httpOnly)
                .secure(secure)
                .path("/")
                .maxAge(maxAgeSeconds)
                .sameSite(sameSite);

        if (domain != null && !domain.trim().isEmpty()) {
            builder.domain(domain.trim());
        }

        return builder.build();
    }

    /**
     * Extracts a cookie value from HttpServletRequest by checking primary and alias names.
     */
    public Optional<String> extractCookie(HttpServletRequest request, String cookieName, String... fallbackNames) {
        if (request.getCookies() == null) {
            return Optional.empty();
        }

        Optional<Cookie> cookie = Arrays.stream(request.getCookies())
                .filter(c -> c.getName().equals(cookieName))
                .findFirst();

        if (cookie.isPresent() && cookie.get().getValue() != null && !cookie.get().getValue().isBlank()) {
            return Optional.of(cookie.get().getValue());
        }

        for (String fallback : fallbackNames) {
            Optional<Cookie> fb = Arrays.stream(request.getCookies())
                    .filter(c -> c.getName().equals(fallback))
                    .findFirst();
            if (fb.isPresent() && fb.get().getValue() != null && !fb.get().getValue().isBlank()) {
                return Optional.of(fb.get().getValue());
            }
        }

        return Optional.empty();
    }
}
