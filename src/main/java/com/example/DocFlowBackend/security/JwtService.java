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

    // Tempo de expiração do Access Token (24 horas)
    private final long ACCESS_TOKEN_EXPIRATION = 86400000; // 24h

    // Tempo de expiração do Refresh Token (7 dias)
    private final long REFRESH_TOKEN_EXPIRATION = 604800000; // 7 dias

    private Key getKey() {
        return Keys.hmacShaKeyFor(SECRET.getBytes());
    }

    // ================= GERAR ACCESS TOKEN =================
    public String generateAccessToken(String subject){
        return generateToken(subject, ACCESS_TOKEN_EXPIRATION);
    }

    // ================= GERAR REFRESH TOKEN =================
    public String generateRefreshToken(String subject){
        return generateToken(subject, REFRESH_TOKEN_EXPIRATION);
    }

    private String generateToken(String subject, long expirationTime){
        Map<String, Object> claims = new HashMap<>();
        return Jwts.builder()
                .setClaims(claims)
                .setSubject(subject)
                .setIssuedAt(new Date(System.currentTimeMillis()))
                .setExpiration(new Date(System.currentTimeMillis() + expirationTime))
                .signWith(getKey())
                .compact();
    }

    // ================= EXTRAIR CLAIMS =================
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

    // ================= SUBJECT =================
    public String extractSubject(String token) {
        return extractClaim(token, Claims::getSubject);
    }

    // ================= EXPIRAÇÃO =================
    public Date extractExpiration(String token) {
        return extractClaim(token, Claims::getExpiration);
    }

    public boolean isTokenExpired(String token){
        return extractExpiration(token).before(new Date());
    }

    // ================= VALIDAÇÃO =================
    public boolean isTokenValid(String token){
        try {
            return !isTokenExpired(token);
        } catch (Exception e){
            return false;
        }
    }
}
