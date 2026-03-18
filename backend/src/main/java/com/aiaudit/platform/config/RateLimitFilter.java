package com.aiaudit.platform.config;

import io.github.bucket4j.Bandwidth;
import io.github.bucket4j.Bucket;
import jakarta.servlet.*;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.time.Duration;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Component
@Order(Ordered.HIGHEST_PRECEDENCE)
@Slf4j
public class RateLimitFilter implements Filter {

    private final Map<String, Bucket> buckets = new ConcurrentHashMap<>();

    @Override
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain)
            throws IOException, ServletException {
        HttpServletRequest httpRequest = (HttpServletRequest) request;
        HttpServletResponse httpResponse = (HttpServletResponse) response;

        String clientIp = getClientIp(httpRequest);
        String path = httpRequest.getRequestURI();

        Bucket bucket = resolveBucket(clientIp, path);
        if (bucket.tryConsume(1)) {
            chain.doFilter(request, response);
        } else {
            log.warn("Rate limit exceeded for IP {} on path {}", clientIp, path);
            httpResponse.setStatus(HttpStatus.TOO_MANY_REQUESTS.value());
            httpResponse.setContentType("application/json");
            httpResponse.getWriter().write("{\"success\":false,\"message\":\"Too many requests. Please try again later.\"}");
        }
    }

    private Bucket resolveBucket(String ip, String path) {
        String key = ip + ":" + classifyPath(path);
        return buckets.computeIfAbsent(key, k -> createBucket(path));
    }

    @SuppressWarnings("deprecation")
    private Bucket createBucket(String path) {
        String normalizedPath = path.replaceFirst("/api/v1", "");

        if (normalizedPath.startsWith("/auth/login")) {
            return Bucket.builder()
                    .addLimit(Bandwidth.simple(5, Duration.ofMinutes(1)))
                    .build();
        } else if (normalizedPath.startsWith("/auth/register")) {
            return Bucket.builder()
                    .addLimit(Bandwidth.simple(3, Duration.ofMinutes(1)))
                    .build();
        } else if (normalizedPath.startsWith("/auth/forgot-password")) {
            return Bucket.builder()
                    .addLimit(Bandwidth.simple(3, Duration.ofMinutes(1)))
                    .build();
        } else {
            return Bucket.builder()
                    .addLimit(Bandwidth.simple(60, Duration.ofMinutes(1)))
                    .build();
        }
    }

    private String classifyPath(String path) {
        String normalized = path.replaceFirst("/api/v1", "");
        if (normalized.startsWith("/auth/login")) return "auth_login";
        if (normalized.startsWith("/auth/register")) return "auth_register";
        if (normalized.startsWith("/auth/forgot-password")) return "auth_forgot";
        return "general";
    }

    private String getClientIp(HttpServletRequest request) {
        String xff = request.getHeader("X-Forwarded-For");
        if (xff != null && !xff.isEmpty()) {
            return xff.split(",")[0].trim();
        }
        return request.getRemoteAddr();
    }
}
