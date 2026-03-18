package com.example.DocFlowBackend.security;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import org.springframework.stereotype.Service;

import java.util.Date;
import java.security.Key;

@Service
public class JwtService {

    private static final String SECRET = "docflow-secret-key-super-safe-123456"; // >= 32 chars

    private Key getKey() {
        return Keys.hmacShaKeyFor(SECRET.getBytes());
    }

    // ================= GERAR TOKEN =================
    public String generateToken(String subject){

        return Jwts.builder()
                .setSubject(subject) // pode ser ID
                .setIssuedAt(new Date())
                .setExpiration(new Date(System.currentTimeMillis() + 86400000)) // 24h
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

    // ================= SUBJECT =================
    public String extractSubject(String token) {
        return extractAllClaims(token).getSubject();
    }

    // ================= EXPIRAÇÃO =================
    public boolean isTokenExpired(String token){
        Date expiration = extractAllClaims(token).getExpiration();
        return expiration.before(new Date());
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