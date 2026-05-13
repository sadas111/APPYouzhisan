package com.chenghua.security;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@Data
@Component
@ConfigurationProperties(prefix = "security.jwt")
public class JwtProperties {
    /**
     * HS256 secret. For production use a long random secret from ENV/secret manager.
     * Must be at least 32 bytes for HS256.
     */
    private String secret;

    /**
     * Token TTL in milliseconds.
     */
    private long expirationMs = 3600_000;

    private String issuer = "chenghua-backend";
}

