package com.delicious.common.security;

import com.delicious.common.exception.RateLimitExceededException;
import jakarta.servlet.http.HttpServletRequest;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Production-grade Rate Limiter:
 * - Primary storage: Distributed Redis (works across scaled microservices & clustered nodes).
 * - Fallback storage: In-memory sliding count map if Redis is unreachable or down, ensuring zero disruption.
 * - Extracts true client IP considering reverse proxies (Cloudflare, AWS ALB, Nginx).
 */
@Slf4j
@Service
public class RateLimiterService {

    private final StringRedisTemplate stringRedisTemplate;

    @Value("${rate-limit.login.max-attempts:3}")
    private int maxAttempts;

    @Value("${rate-limit.login.window-seconds:120}")
    private long windowSeconds;

    // Resilient in-memory fallback cache in case Redis is offline
    private final Map<String, InMemoryAttempt> fallbackCache = new ConcurrentHashMap<>();

    public RateLimiterService(StringRedisTemplate stringRedisTemplate) {
        this.stringRedisTemplate = stringRedisTemplate;
    }

    /**
     * Checks and increments rate-limiting for login attempts.
     * Enforces maxAttempts within windowSeconds per client IP.
     *
     * @param request HttpServletRequest to extract client IP
     * @throws RateLimitExceededException if user exceeds the allowed attempts
     */
    public void checkLoginRateLimit(HttpServletRequest request) {
        String clientIp = extractClientIp(request);
        String redisKey = "rate_limit:login:ip:" + clientIp;

        try {
            // Attempt with Redis
            Long currentAttempts = stringRedisTemplate.opsForValue().increment(redisKey);

            if (currentAttempts != null && currentAttempts == 1) {
                // First attempt in this window: set TTL
                stringRedisTemplate.expire(redisKey, Duration.ofSeconds(windowSeconds));
            }

            if (currentAttempts != null && currentAttempts > maxAttempts) {
                Long ttl = stringRedisTemplate.getExpire(redisKey);
                long retryAfter = (ttl != null && ttl > 0) ? ttl : windowSeconds;

                String formattedTime = formatWaitTime(retryAfter);
                log.warn("Rate limit exceeded for IP {} on login: {} attempts. Retry after {}s", clientIp, currentAttempts, retryAfter);

                throw new RateLimitExceededException(
                        String.format("Too many login attempts. You have exceeded the limit of %d attempts in %d minutes. Please wait %s before trying again.",
                                maxAttempts,
                                windowSeconds / 60,
                                formattedTime),
                        retryAfter
                );
            }
        } catch (RateLimitExceededException ex) {
            throw ex;
        } catch (Exception ex) {
            // Redis error or connection failure: Log warning and fallback gracefully to in-memory limiter
            log.warn("Redis unavailable during login rate check ({}). Falling back to in-memory rate limiting for IP {}.",
                    ex.getMessage(), clientIp);
            checkInMemoryRateLimit(clientIp);
        }
    }

    /**
     * Resets rate-limiting attempts on successful login.
     */
    public void resetLoginRateLimit(HttpServletRequest request) {
        String clientIp = extractClientIp(request);
        String redisKey = "rate_limit:login:ip:" + clientIp;
        try {
            stringRedisTemplate.delete(redisKey);
        } catch (Exception ex) {
            log.debug("Could not reset Redis rate limit for IP {}: {}", clientIp, ex.getMessage());
        }
        fallbackCache.remove(clientIp);
    }

    /**
     * In-memory fallback if Redis is down.
     */
    private void checkInMemoryRateLimit(String clientIp) {
        long now = System.currentTimeMillis();
        long windowMillis = windowSeconds * 1000L;

        InMemoryAttempt attempt = fallbackCache.compute(clientIp, (key, existing) -> {
            if (existing == null || now > existing.expiresAt) {
                return new InMemoryAttempt(1, now + windowMillis);
            }
            existing.count++;
            return existing;
        });

        if (attempt.count > maxAttempts) {
            long remainingMillis = Math.max(0, attempt.expiresAt - now);
            long retryAfterSeconds = Math.max(1, remainingMillis / 1000);
            String formattedTime = formatWaitTime(retryAfterSeconds);

            throw new RateLimitExceededException(
                    String.format("Too many login attempts. You have exceeded the limit of %d attempts in %d minutes. Please wait %s before trying again.",
                            maxAttempts,
                            windowSeconds / 60,
                            formattedTime),
                    retryAfterSeconds
            );
        }
    }

    /**
     * Extracts client IP address behind reverse proxies (Nginx, AWS ELB, Cloudflare).
     */
    public String extractClientIp(HttpServletRequest request) {
        if (request == null) {
            return "unknown";
        }

        String[] headers = {
                "CF-Connecting-IP",    // Cloudflare
                "X-Forwarded-For",     // Standard proxy
                "X-Real-IP",           // Nginx
                "Proxy-Client-IP",
                "WL-Proxy-Client-IP"
        };

        for (String header : headers) {
            String ip = request.getHeader(header);
            if (ip != null && !ip.isBlank() && !"unknown".equalsIgnoreCase(ip.trim())) {
                // X-Forwarded-For may contain multiple comma-separated IPs (client, proxy1, proxy2)
                if (ip.contains(",")) {
                    ip = ip.split(",")[0].trim();
                }
                return ip;
            }
        }

        return request.getRemoteAddr() != null ? request.getRemoteAddr() : "127.0.0.1";
    }

    private String formatWaitTime(long seconds) {
        if (seconds < 60) {
            return seconds + " second" + (seconds == 1 ? "" : "s");
        }
        long minutes = seconds / 60;
        long remSeconds = seconds % 60;
        if (remSeconds == 0) {
            return minutes + " minute" + (minutes == 1 ? "" : "s");
        }
        return String.format("%d minute%s %d second%s",
                minutes, minutes == 1 ? "" : "s",
                remSeconds, remSeconds == 1 ? "" : "s");
    }

    private static class InMemoryAttempt {
        int count;
        long expiresAt;

        InMemoryAttempt(int count, long expiresAt) {
            this.count = count;
            this.expiresAt = expiresAt;
        }
    }
}
