package com.delicious.common.security;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.lang.NonNull;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

/**
 * JWT Authentication Filter — runs once per request.
 *
 * Flow:
 *  1. Extract "Bearer <token>" from the Authorization header.
 *  2. Validate the token signature and expiration via JwtService.
 *  3. Load the UserDetails and set the SecurityContext so that
 *     Spring Security treats the request as authenticated.
 *
 * If any step fails (missing header, invalid token, unknown user),
 * the filter simply passes the request along without authenticating —
 * downstream security rules will then deny access to protected routes.
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class JwtAuthFilter extends OncePerRequestFilter {

    private final JwtService jwtService;
    private final CustomUserDetailsService userDetailsService;
    private final CookieService cookieService;

    @Override
    protected void doFilterInternal(
            @NonNull HttpServletRequest request,
            @NonNull HttpServletResponse response,
            @NonNull FilterChain filterChain
    ) throws ServletException, IOException {

        final String authHeader = request.getHeader("Authorization");

        String jwt = null;
        if (authHeader != null && authHeader.startsWith("Bearer ")) {
            jwt = authHeader.substring(7); // Strip "Bearer " prefix
        } else {
            // Check cookie if Authorization header is not present
            jwt = cookieService.extractCookie(request, CookieService.ACCESS_TOKEN_COOKIE_NAME, "accessToken")
                    .orElse(null);
        }

        // If no token found, skip authentication — let the filter chain continue
        if (jwt == null || jwt.isBlank()) {
            filterChain.doFilter(request, response);
            return;
        }

        try {
            // Ensure this is an access token, not a refresh token
            if (jwtService.isAccessToken(jwt)) {
                final String email = jwtService.extractEmail(jwt);

            // Only authenticate if not already authenticated in this request
            if (email != null && SecurityContextHolder.getContext().getAuthentication() == null) {
                UserDetails userDetails = userDetailsService.loadUserByUsername(email);

                if (jwtService.isTokenValid(jwt, userDetails)) {
                    UsernamePasswordAuthenticationToken authToken =
                            new UsernamePasswordAuthenticationToken(
                                    userDetails,
                                    null,
                                    userDetails.getAuthorities()
                            );
                    authToken.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));
                    // Set authentication in the SecurityContext for this request
                    SecurityContextHolder.getContext().setAuthentication(authToken);
                    log.debug("JWT authentication successful for user: {}", email);
                }
            }
        }
    } catch (Exception e) {
            // Log at debug level — invalid tokens are a normal occurrence (expired, tampered, etc.)
            log.debug("JWT validation failed: {}", e.getMessage());
        }

        filterChain.doFilter(request, response);
    }
}
