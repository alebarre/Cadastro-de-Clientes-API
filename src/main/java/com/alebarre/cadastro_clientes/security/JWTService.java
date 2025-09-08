package com.alebarre.cadastro_clientes.security;

import io.jsonwebtoken.*;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import java.security.Key;
import java.util.Date;
import java.util.Map;
import java.util.function.Function;

@Service
public class JWTService {
    private final Key key;
    private final long expirationMs;

    public JWTService(@Value("${jwt.secret}") String secret,
                      @Value("${jwt.expiration-ms}") long expirationMs) {
        this.key = Keys.hmacShaKeyFor(Decoders.BASE64.decode(secret));
        this.expirationMs = expirationMs;
    }

    public String extractUsername(String token) {
        return extractClaim(token, Claims::getSubject);
    }

    public <T> T extractClaim(String token, Function<Claims,T> resolver) {
        final Claims claims = Jwts.parserBuilder().setSigningKey(key).build()
                .parseClaimsJws(token).getBody();
        return resolver.apply(claims);
    }

    public String generateToken(String username, String roles) {
        long now = System.currentTimeMillis();
        return Jwts.builder()
                .setSubject(username)
                .addClaims(Map.of("roles", roles))
                .setIssuedAt(new Date(now))
                .setExpiration(new Date(now + expirationMs))
                .signWith(key, SignatureAlgorithm.HS256)
                .compact();
    }

    public boolean isValid(String token, String username) {
        try {
            return extractUsername(token).equals(username) && !isExpired(token);
        } catch (Exception e) { return false; }
    }

    public boolean isExpired(String token) {
        Date exp = extractClaim(token, Claims::getExpiration);
        return exp.before(new Date());
    }
}

