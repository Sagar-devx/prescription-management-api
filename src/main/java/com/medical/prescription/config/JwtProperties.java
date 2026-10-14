package com.medical.prescription.config;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

@Configuration
@ConfigurationProperties(prefix = "jwt")
public class JwtProperties {

    private String secret = "a-very-long-secret-key-for-jwt-signing-must-be-at-least-256-bits-long-for-hs256";
    private long expiration = 900000L;

    public JwtProperties() {}

    public JwtProperties(String secret, long expiration) {
        this.secret = secret;
        this.expiration = expiration;
    }

    // Record-style accessors for backward compatibility
    public String secret() {
        return secret;
    }

    public long expiration() {
        return expiration;
    }

    // Standard JavaBeans getters & setters for Spring Boot property binding
    public String getSecret() {
        return secret;
    }

    public void setSecret(String secret) {
        this.secret = secret;
    }

    public long getExpiration() {
        return expiration;
    }

    public void setExpiration(long expiration) {
        this.expiration = expiration;
    }
}
