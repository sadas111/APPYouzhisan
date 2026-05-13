package com.chenghua.security;

import jakarta.servlet.http.HttpServletRequest;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.Deque;
import java.util.List;
import java.util.concurrent.ConcurrentLinkedDeque;
import java.util.concurrent.atomic.AtomicLong;

@Service
public class CrawlerLogService {

    private static final int MAX_LOG_SIZE = 200;

    private final Deque<CrawlerLogEntry> logs = new ConcurrentLinkedDeque<>();
    private final AtomicLong idGenerator = new AtomicLong(0);

    public void recordBlockedRequest(HttpServletRequest request, String reason, String ip) {
        long id = idGenerator.incrementAndGet();
        long now = System.currentTimeMillis();

        String userAgent = request.getHeader("User-Agent");
        String path = request.getRequestURI();

        CrawlerLogEntry entry = new CrawlerLogEntry(
                id,
                ip,
                userAgent,
                path,
                now,
                reason
        );

        logs.addFirst(entry);
        while (logs.size() > MAX_LOG_SIZE) {
            logs.removeLast();
        }
    }

    public List<CrawlerLogEntry> getRecentLogs() {
        return new ArrayList<>(logs);
    }
}

