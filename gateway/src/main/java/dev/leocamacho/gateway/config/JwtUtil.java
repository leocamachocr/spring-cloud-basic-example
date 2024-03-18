package dev.leocamacho.gateway.config;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import jakarta.annotation.PostConstruct;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.crypto.SecretKey;
import java.util.Date;

@Component
public class JwtUtil {

    @Value("${jwt.secret}")
    private String secret;

    private SecretKey key;
    public static final String AUTHORIZATION_PREFIX = "Bearer ";

    @PostConstruct
    public void init() {
        this.key = Keys.hmacShaKeyFor(secret.getBytes());
    }

    public Claims getAllClaimsFromToken(String token) {
        return Jwts.parser().verifyWith(key).build().parseSignedClaims(token).getPayload();
    }

    private boolean isTokenExpired(String token) {
        return getAllClaimsFromToken(token).getExpiration().before(new Date());
    }

    public boolean isInvalid(String token) {
        try {
            return this.isTokenExpired(token);
        } catch (Exception e) {
            return true;
        }
    }

    public String getToken(String authorizationHeader) {
        if (authorizationHeader == null) {
            return null;
        } else {
            if (AUTHORIZATION_PREFIX.length() <= authorizationHeader.length())
                return authorizationHeader.substring(AUTHORIZATION_PREFIX.length());
            else return null;
        }
    }


}