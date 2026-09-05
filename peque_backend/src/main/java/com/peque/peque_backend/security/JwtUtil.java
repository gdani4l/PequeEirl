package com.peque.peque_backend.security;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import java.nio.charset.StandardCharsets;
import java.util.Date;
import javax.crypto.SecretKey;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

@Component
public class JwtUtil {

    @Value("${app.jwt.secret}")
    private String secret;

    @Value("${app.jwt.expiration-hours}")
    private long expirationHours;

    private SecretKey getKey() {
        return Keys.hmacShaKeyFor(secret.getBytes(StandardCharsets.UTF_8));
    }

    public String generarToken(Integer idUsuario, String correo, String rol) {
        Date ahora = new Date();
        Date expira = new Date(ahora.getTime() + expirationHours * 60 * 60 * 1000);
        return Jwts.builder()
                .subject(String.valueOf(idUsuario))
                .claim("correo", correo)
                .claim("rol", rol)
                .issuedAt(ahora)
                .expiration(expira)
                .signWith(getKey())
                .compact();
    }

    public Claims validarToken(String token) {
        return Jwts.parser()
                .verifyWith(getKey())
                .build()
                .parseSignedClaims(token)
                .getPayload();
    }
}
