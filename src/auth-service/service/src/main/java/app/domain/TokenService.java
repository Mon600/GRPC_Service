package app.domain;


import app.exceptions.InvalidTokenException;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import javax.crypto.SecretKey;
import java.util.Date;
import java.util.UUID;


@Service
public class TokenService {
    @Value("${SECRET}")
    private String secret;

    @Value("${EXPIRATION_TIME}")
    private long expirationTimeMs;

    private SecretKey getSigningKey() {
        return Keys.hmacShaKeyFor(secret.getBytes());
    }


    public String generateToken(UUID userId, String email) {
        Date now = new Date();
        Date expirationDate = new Date(now.getTime() + expirationTimeMs);
        return Jwts.builder()
            .subject(userId.toString())
            .claim("email", email)
            .issuedAt(now)
            .expiration(expirationDate)
            .signWith(getSigningKey())
            .compact();

    }

    public Claims validateToken(String token) {
        try {
            return Jwts.parser()
                .verifyWith(getSigningKey())
                .build()
                .parseSignedClaims(token)
                .getPayload();
        } catch (JwtException e) {
            throw new InvalidTokenException("Invalid token", e);
        }
    }
}
