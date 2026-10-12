package com.delicious.domain.user.controller;

import com.delicious.common.response.ApiResponse;
import com.delicious.common.security.CookieService;
import com.delicious.domain.user.dto.AuthResponse;
import com.delicious.domain.user.dto.LoginRequest;
import com.delicious.domain.user.dto.RefreshTokenRequest;
import com.delicious.domain.user.dto.RegisterRequest;
import com.delicious.domain.user.exception.InvalidCredentialsException;
import com.delicious.domain.user.service.AuthService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseCookie;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/auth")
@RequiredArgsConstructor
@Tag(name = "Authentication", description = "Endpoints for user registration, login, refresh token, and logout")
public class AuthController {

    private final AuthService authService;
    private final CookieService cookieService;

    @Operation(summary = "Register a new user", description = "Creates a new user account with hashed password and sets auth cookies")
    @PostMapping("/register")
    public ResponseEntity<ApiResponse<AuthResponse>> register(@Valid @RequestBody RegisterRequest request) {
        AuthResponse response = authService.registerUser(request);

        ResponseCookie accessCookie = cookieService.createAccessTokenCookie(response.getToken());
        ResponseCookie refreshCookie = cookieService.createRefreshTokenCookie(response.getRefreshToken());
        ResponseCookie roleCookie = cookieService.createUserRoleCookie(response.getRole().name());

        return ResponseEntity.status(HttpStatus.CREATED)
                .header(HttpHeaders.SET_COOKIE, accessCookie.toString())
                .header(HttpHeaders.SET_COOKIE, refreshCookie.toString())
                .header(HttpHeaders.SET_COOKIE, roleCookie.toString())
                .body(ApiResponse.success(response, "User registered successfully"));
    }

    @Operation(summary = "Login user", description = "Authenticates a user, returns their details, and sets auth cookies")
    @PostMapping("/login")
    public ResponseEntity<ApiResponse<AuthResponse>> login(@Valid @RequestBody LoginRequest request) {
        AuthResponse response = authService.loginUser(request);

        ResponseCookie accessCookie = cookieService.createAccessTokenCookie(response.getToken());
        ResponseCookie refreshCookie = cookieService.createRefreshTokenCookie(response.getRefreshToken());
        ResponseCookie roleCookie = cookieService.createUserRoleCookie(response.getRole().name());

        return ResponseEntity.ok()
                .header(HttpHeaders.SET_COOKIE, accessCookie.toString())
                .header(HttpHeaders.SET_COOKIE, refreshCookie.toString())
                .header(HttpHeaders.SET_COOKIE, roleCookie.toString())
                .body(ApiResponse.success(response, "User logged in successfully"));
    }

    @Operation(summary = "Refresh access token", description = "Generates a new access token using a refresh token from cookies or body")
    @PostMapping({"/refresh-token", "/refresh"})
    public ResponseEntity<ApiResponse<AuthResponse>> refreshToken(
            @RequestBody(required = false) RefreshTokenRequest requestBody,
            HttpServletRequest request
    ) {
        // Look up refresh token: 1) HttpOnly cookie, 2) request body, 3) Authorization header
        String refreshToken = cookieService.extractCookie(
                request,
                CookieService.REFRESH_TOKEN_COOKIE_NAME,
                "refreshToken"
        ).orElse(null);

        if (refreshToken == null && requestBody != null && requestBody.getRefreshToken() != null) {
            refreshToken = requestBody.getRefreshToken();
        }

        if (refreshToken == null) {
            final String authHeader = request.getHeader("Authorization");
            if (authHeader != null && authHeader.startsWith("Bearer ")) {
                refreshToken = authHeader.substring(7);
            }
        }

        if (refreshToken == null || refreshToken.isBlank()) {
            throw new InvalidCredentialsException("Refresh token is required in cookie or request body");
        }

        AuthResponse response = authService.refreshToken(refreshToken);

        ResponseCookie accessCookie = cookieService.createAccessTokenCookie(response.getToken());
        ResponseCookie refreshCookie = cookieService.createRefreshTokenCookie(response.getRefreshToken());
        ResponseCookie roleCookie = cookieService.createUserRoleCookie(response.getRole().name());

        return ResponseEntity.ok()
                .header(HttpHeaders.SET_COOKIE, accessCookie.toString())
                .header(HttpHeaders.SET_COOKIE, refreshCookie.toString())
                .header(HttpHeaders.SET_COOKIE, roleCookie.toString())
                .body(ApiResponse.success(response, "Token refreshed successfully"));
    }

    @Operation(summary = "Logout user", description = "Clears all auth cookies on the client")
    @PostMapping("/logout")
    public ResponseEntity<ApiResponse<Void>> logout() {
        ResponseCookie cleanAccess = cookieService.createCleanAccessTokenCookie();
        ResponseCookie cleanRefresh = cookieService.createCleanRefreshTokenCookie();
        ResponseCookie cleanRole = cookieService.createCleanUserRoleCookie();

        return ResponseEntity.ok()
                .header(HttpHeaders.SET_COOKIE, cleanAccess.toString())
                .header(HttpHeaders.SET_COOKIE, cleanRefresh.toString())
                .header(HttpHeaders.SET_COOKIE, cleanRole.toString())
                .body(ApiResponse.success(null, "User logged out successfully"));
    }
}
