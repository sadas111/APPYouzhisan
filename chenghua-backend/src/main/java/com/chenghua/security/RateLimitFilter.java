package com.chenghua.security;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.http.MediaType;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicLong;

@Component
public class RateLimitFilter extends OncePerRequestFilter {

    private static final long WINDOW_MS = Duration.ofMinutes(1).toMillis();
    private static final long STALE_MS = Duration.ofMinutes(10).toMillis();

    private static final int API_LIMIT_PER_MINUTE = 120;
    private static final int LOGIN_LIMIT_PER_MINUTE = 10;

    private final ConcurrentHashMap<String, Window> windows = new ConcurrentHashMap<>();
    private final AtomicLong calls = new AtomicLong(0);

    private final CrawlerLogService crawlerLogService;

    public RateLimitFilter(CrawlerLogService crawlerLogService) {
        this.crawlerLogService = crawlerLogService;
    }

    private static final class Window {
        private long startMs;
        private int count;
        private long lastSeenMs;

        private Window(long startMs) {
            this.startMs = startMs;
            this.count = 0;
            this.lastSeenMs = startMs;
        }
    }

    @Override
    protected boolean shouldNotFilter(HttpServletRequest request) {
        String uri = request.getRequestURI();
        if (uri == null) {
            return true;
        }
        // 安全中心查看爬虫日志等接口不参与限流，避免自查接口本身被 429 拦截
        if (uri.startsWith("/api/security/")) {
            return true;
        }
        return !uri.startsWith("/api/");
    }

    @Override
    protected void doFilterInternal(
            HttpServletRequest request,
            HttpServletResponse response,
            FilterChain filterChain
    ) throws ServletException, IOException {

        long now = System.currentTimeMillis();
        boolean isLogin = "POST".equalsIgnoreCase(request.getMethod()) && "/api/users/login".equals(request.getRequestURI());

        String ip = clientIp(request);
        String key = isLogin
                ? "login:" + ip
                : "api:" + principalOrIp(request, ip);

        int limit = isLogin ? LOGIN_LIMIT_PER_MINUTE : API_LIMIT_PER_MINUTE;
        if (!allow(key, now, limit)) {
            String reason = isLogin ? "high_login_rate" : "high_request_rate";
            crawlerLogService.recordBlockedRequest(request, reason, ip);
            response.setStatus(429);
            response.setCharacterEncoding(StandardCharsets.UTF_8.name());
            response.setContentType(MediaType.APPLICATION_JSON_VALUE);
            response.getWriter().write("{\"message\":\"Too many requests\"}");
            return;
        }

        cleanupOccasionally(now);
        filterChain.doFilter(request, response);
    }

    private boolean allow(String key, long nowMs, int limitPerWindow) {
        Window w = windows.compute(key, (k, existing) -> {
            if (existing == null) {
                return new Window(nowMs);
            }
            if (nowMs - existing.startMs >= WINDOW_MS) {
                existing.startMs = nowMs;
                existing.count = 0;
            }
            existing.count += 1;
            existing.lastSeenMs = nowMs;
            return existing;
        });
        return w.count <= limitPerWindow;
    }

    private void cleanupOccasionally(long nowMs) {
        long n = calls.incrementAndGet();
        if (n % 1000 != 0) {
            return;
        }
        for (Map.Entry<String, Window> e : windows.entrySet()) {
            Window w = e.getValue();
            if (w == null) continue;
            if (nowMs - w.lastSeenMs > STALE_MS) {
                windows.remove(e.getKey(), w);
            }
        }
    }

    private static String principalOrIp(HttpServletRequest request, String ip) {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth != null && auth.isAuthenticated()) {
            String name = auth.getName();
            if (name != null && !name.isBlank() && !"anonymousUser".equals(name)) {
                return "u:" + name;
            }
        }
        return "ip:" + ip;
    }

    private static String clientIp(HttpServletRequest request) {
        String xff = request.getHeader("X-Forwarded-For");
        if (xff != null && !xff.isBlank()) {
            int comma = xff.indexOf(',');
            return (comma > 0 ? xff.substring(0, comma) : xff).trim();
        }
        String realIp = request.getHeader("X-Real-IP");
        if (realIp != null && !realIp.isBlank()) {
            return realIp.trim();
        }
        String remoteAddr = request.getRemoteAddr();
        return remoteAddr == null ? "unknown" : remoteAddr;
    }
}

