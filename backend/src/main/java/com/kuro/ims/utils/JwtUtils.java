package com.kuro.ims.utils;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

@Component
public class JwtUtils {

    @Value("${jwt.secret}")
    private String secret;

    @Value("${jwt.expire}")
    private Long expire;

    @Value("${jwt.refresh-expire}")
    private Long refreshExpire;

    private SecretKey getKey() {
        return Keys.hmacShaKeyFor(secret.getBytes(StandardCharsets.UTF_8));
    }

    public String createToken(Long userId, String username, String role) {
        return buildToken(userId, username, role, expire, "access");
    }

    public String createRefreshToken(Long userId, String username, String role) {
        return buildToken(userId, username, role, refreshExpire, "refresh");
    }

    private String buildToken(Long userId, String username, String role, Long ttl, String type) {
        Map<String, Object> claims = new HashMap<>();
        claims.put("userId", userId);
        claims.put("username", username);
        claims.put("role", role);
        claims.put("type", type);
        Date now = new Date();
        return Jwts.builder()
                .claims(claims)
                .issuedAt(now)
                .expiration(new Date(now.getTime() + ttl))
                .signWith(getKey())
                .compact();
    }

    public Claims parseToken(String token) {
        try {
            return Jwts.parser()
                    .verifyWith(getKey())
                    .build()
                    .parseSignedClaims(token)
                    .getPayload();
        } catch (Exception e) {
            return null;
        }
    }

    public boolean isExpired(String token) {
        Claims claims = parseToken(token);
        return claims == null || claims.getExpiration().before(new Date());
    }

    public Long getExpire() {
        return expire;
    }
}
