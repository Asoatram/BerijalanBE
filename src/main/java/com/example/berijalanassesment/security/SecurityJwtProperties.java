package com.example.berijalanassesment.security;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "security.jwt")
public class SecurityJwtProperties {

    private String secret = "";
    private String issuer = "vigigate-api";
    private long accessTtlSeconds = 3600;
    private long refreshTtlSeconds = 604800;
    private long refreshRemembermeTtlSeconds = 2592000;

    public String getSecret() {
        return secret;
    }

    public void setSecret(String secret) {
        this.secret = secret;
    }

    public String getIssuer() {
        return issuer;
    }

    public void setIssuer(String issuer) {
        this.issuer = issuer;
    }

    public long getAccessTtlSeconds() {
        return accessTtlSeconds;
    }

    public void setAccessTtlSeconds(long accessTtlSeconds) {
        this.accessTtlSeconds = accessTtlSeconds;
    }

    public long getRefreshTtlSeconds() {
        return refreshTtlSeconds;
    }

    public void setRefreshTtlSeconds(long refreshTtlSeconds) {
        this.refreshTtlSeconds = refreshTtlSeconds;
    }

    public long getRefreshRemembermeTtlSeconds() {
        return refreshRemembermeTtlSeconds;
    }

    public void setRefreshRemembermeTtlSeconds(long refreshRemembermeTtlSeconds) {
        this.refreshRemembermeTtlSeconds = refreshRemembermeTtlSeconds;
    }
}
