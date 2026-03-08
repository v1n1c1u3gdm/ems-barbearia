package com.emsbarbearia.service;

import com.emsbarbearia.config.JwtPublicProperties;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import java.nio.charset.StandardCharsets;
import java.util.Date;
import javax.crypto.SecretKey;
import org.springframework.stereotype.Service;

@Service
public class JwtPublicService {

    private static final String TYP_CLAIM = "typ";
    private static final String TYP_PUBLIC = "public";
    private static final String ISSUER = "ems-barbearia-public";

    private final JwtPublicProperties properties;

    public JwtPublicService(JwtPublicProperties properties) {
        this.properties = properties;
    }

    public String createToken(Long clienteId) {
        SecretKey key = Keys.hmacShaKeyFor(properties.getPublicSecret().getBytes(StandardCharsets.UTF_8));
        return Jwts.builder()
            .subject(String.valueOf(clienteId))
            .claim(TYP_CLAIM, TYP_PUBLIC)
            .issuer(ISSUER)
            .issuedAt(new Date())
            .expiration(new Date(System.currentTimeMillis() + properties.getPublicExpirationMs()))
            .signWith(key)
            .compact();
    }

    public Long parseClienteId(String token) {
        SecretKey key = Keys.hmacShaKeyFor(properties.getPublicSecret().getBytes(StandardCharsets.UTF_8));
        Claims claims = Jwts.parser()
            .verifyWith(key)
            .requireIssuer(ISSUER)
            .require(TYP_CLAIM, TYP_PUBLIC)
            .build()
            .parseSignedClaims(token)
            .getPayload();
        return Long.parseLong(claims.getSubject());
    }

    public boolean isValid(String token) {
        try {
            parseClienteId(token);
            return true;
        } catch (io.jsonwebtoken.JwtException e) {
            return false;
        }
    }
}
