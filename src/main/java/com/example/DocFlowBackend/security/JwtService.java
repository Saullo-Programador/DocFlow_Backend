package com.example.DocFlowBackend.security;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.util.Date;
import java.security.Key;
import java.util.HashMap;
import java.util.Map;
import java.util.function.Function;

@Service
public class JwtService {

    @Value("${jwt.secret}")
    private String SECRET;

    private final long ACCESS_TOKEN_EXPIRATION = 86400000; // 24h
    private final long REFRESH_TOKEN_EXPIRATION = 604800000; // 7 dias

    private Key getKey() {
        return Keys.hmacShaKeyFor(SECRET.getBytes());
    }

    public String generateAccessToken(String subject, String role){
        Map<String, Object> claims = new HashMap<>();
        claims.put("role", role); // Inclui a role no payload
        return generateToken(subject, claims, ACCESS_TOKEN_EXPIRATION);
    }

    public String generateRefreshToken(String subject){
        return generateToken(subject, new HashMap<>(), REFRESH_TOKEN_EXPIRATION);
    }

    private String generateToken(String subject, Map<String, Object> claims, long expirationTime){
        return Jwts.builder()
                .setClaims(claims)
                .setSubject(subject)
                .setIssuedAt(new Date(System.currentTimeMillis()))
                .setExpiration(new Date(System.currentTimeMillis() + expirationTime))
                .signWith(getKey())
                .compact();
    }

    private Claims extractAllClaims(String token){
        return Jwts.parserBuilder()
                .setSigningKey(getKey())
                .build()
                .parseClaimsJws(token)
                .getBody();
    }

    public <T> T extractClaim(String token, Function<Claims, T> claimsResolver) {
        final Claims claims = extractAllClaims(token);
        return claimsResolver.apply(claims);
    }

    public String extractSubject(String token) {
        return extractClaim(token, Claims::getSubject);
    }

    public String extractRole(String token) {
        return (String) extractAllClaims(token).get("role");
    }

    public Date extractExpiration(String token) {
        return extractClaim(token, Claims::getExpiration);
    }

    public boolean isTokenExpired(String token){
        return extractExpiration(token).before(new Date());
    }

    public boolean isTokenValid(String token){
        try {
            return !isTokenExpired(token);
        } catch (Exception e){
            return false;
        }
    }
}
