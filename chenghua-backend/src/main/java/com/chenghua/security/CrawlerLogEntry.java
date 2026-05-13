package com.chenghua.security;

public class CrawlerLogEntry {

    private final long id;
    private final String ip;
    private final String userAgent;
    private final String path;
    private final long detectedAt;
    private final String reason;

    public CrawlerLogEntry(long id, String ip, String userAgent, String path, long detectedAt, String reason) {
        this.id = id;
        this.ip = ip;
        this.userAgent = userAgent;
        this.path = path;
        this.detectedAt = detectedAt;
        this.reason = reason;
    }

    public long getId() {
        return id;
    }

    public String getIp() {
        return ip;
    }

    public String getUserAgent() {
        return userAgent;
    }

    public String getPath() {
        return path;
    }

    public long getDetectedAt() {
        return detectedAt;
    }

    public String getReason() {
        return reason;
    }
}

