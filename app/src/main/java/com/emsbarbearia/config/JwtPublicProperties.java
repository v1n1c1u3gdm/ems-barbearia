package com.emsbarbearia.config;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@Component
@ConfigurationProperties(prefix = "app.jwt")
public class JwtPublicProperties {

    private String publicSecret = "default-public-secret-change-in-production";
    private long publicExpirationMs = 86400000L;

    public String getPublicSecret() {
        return publicSecret;
    }

    public void setPublicSecret(String publicSecret) {
        this.publicSecret = publicSecret;
    }

    public long getPublicExpirationMs() {
        return publicExpirationMs;
    }

    public void setPublicExpirationMs(long publicExpirationMs) {
        this.publicExpirationMs = publicExpirationMs;
    }
}
