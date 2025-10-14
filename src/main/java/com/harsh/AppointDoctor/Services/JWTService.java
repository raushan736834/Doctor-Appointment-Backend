package com.harsh.AppointDoctor.Services;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;

import java.util.function.Function;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Service;

import javax.crypto.SecretKey;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

@Service
public class JWTService {

    @Value("${jwt.secret}")
    private String secretKey;

    @Value("${jwt.access.expiration:900000}") // 15 minutes default
    private long accessTokenExpiration;

    @Value("${jwt.refresh.expiration:604800000}") // 7 days default
    private long refreshTokenExpiration;

    // Store active refresh tokens (in production, use Redis or database)
    private final Set<String> activeRefreshTokens = ConcurrentHashMap.newKeySet();

    public String generateAccessToken(String email) {
        Map<String, Object> claims = new HashMap<>();
        claims.put("type", "access");

        return Jwts.builder()
                .claims()
                .add(claims)
                .subject(email)
                .issuedAt(new Date(System.currentTimeMillis()))
                .expiration(new Date(System.currentTimeMillis() + accessTokenExpiration))
                .and()
                .signWith(getKey())
                .compact();
    }

    public String generateRefreshToken(String email) {
        String tokenId = UUID.randomUUID().toString();
        Map<String, Object> claims = new HashMap<>();
        claims.put("type", "refresh");
        claims.put("tokenId", tokenId);

        String token = Jwts.builder()
                .claims()
                .add(claims)
                .subject(email)
                .issuedAt(new Date(System.currentTimeMillis()))
                .expiration(new Date(System.currentTimeMillis() + refreshTokenExpiration))
                .and()
                .signWith(getKey())
                .compact();

        // Store token ID for validation
        activeRefreshTokens.add(tokenId);
        return token;
    }

    private SecretKey getKey() {
        byte[] keyBytes = Decoders.BASE64.decode(secretKey);
        return Keys.hmacShaKeyFor(keyBytes);
    }

    public String extractEmail(String token) {
        return extractClaim(token, Claims::getSubject);
    }

    public String extractTokenType(String token) {
        return extractClaim(token, claims -> claims.get("type", String.class));
    }

    public String extractTokenId(String token) {
        return extractClaim(token, claims -> claims.get("tokenId", String.class));
    }

    private <T> T extractClaim(String token, Function<Claims, T> claimsResolver) {
        final Claims claims = extractAllClaims(token);
        return claimsResolver.apply(claims);
    }

    private Claims extractAllClaims(String token) {
        return Jwts.parser()
                .verifyWith(getKey())
                .build()
                .parseSignedClaims(token)
                .getPayload();
    }

    public boolean validateAccessToken(String token, UserDetails userDetails) {
        final String email = extractEmail(token);
        final String tokenType = extractTokenType(token);
        return (email.equals(userDetails.getUsername())
                && "access".equals(tokenType)
                && !isTokenExpired(token));
    }

    public boolean validateRefreshToken(String token) {
        try {
            final String tokenType = extractTokenType(token);
            final String tokenId = extractTokenId(token);
            return "refresh".equals(tokenType)
                    && !isTokenExpired(token)
                    && activeRefreshTokens.contains(tokenId);
        } catch (Exception e) {
            return false;
        }
    }

    public void invalidateRefreshToken(String token) {
        try {
            String tokenId = extractTokenId(token);
            if (tokenId != null) {
                activeRefreshTokens.remove(tokenId);
            }
        } catch (Exception e) {
            // Token might be malformed, ignore
        }
    }

    public void invalidateAllRefreshTokensForUser(String email) {
        // In a real implementation, you'd query the database for all user's refresh tokens
        // For now, this is a placeholder
        activeRefreshTokens.clear();
    }

    private boolean isTokenExpired(String token) {
        return extractExpiration(token).before(new Date());
    }

    private Date extractExpiration(String token) {
        return extractClaim(token, Claims::getExpiration);
    }
}