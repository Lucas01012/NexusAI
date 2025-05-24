package com.meialuaquadrado.nexusai.adapters.in.security;

import java.nio.charset.StandardCharsets;
import java.util.Date;
import java.util.HashMap;

import org.springframework.security.core.Authentication;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.AuthenticationCredentialsNotFoundException;
import org.springframework.stereotype.Component;

import com.meialuaquadrado.nexusai.adapters.in.repositories.UserRepository;
import com.meialuaquadrado.nexusai.adapters.in.User;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.security.Keys;

import java.util.Map;

@Component
public class JwtGenerator {

    private final UserRepository userRepository;

    @Autowired
    public JwtGenerator(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    public String generateToken(Authentication authentication) {
        String username = authentication.getName();
        Date now = new Date();
        Date expiry = new Date(now.getTime() + SecurityConstants.JWT_EXPIRATION);

        // 1) Busca sua entidade real no banco
        User domainUser = userRepository.findByUsername(username)
                .orElseThrow(() -> new RuntimeException("Usuário não encontrado no DB"));

        // 2) Monta as claims
        Map<String, Object> claims = new HashMap<>();
        claims.put("username", domainUser.getUsername());
        claims.put("id", domainUser.getId());
        claims.put("email", domainUser.getEmail());

        // 3) Gera o token
        return Jwts.builder()
                .claims(claims)
                .subject(username)
                .issuedAt(now)
                .expiration(expiry)
                .signWith(Keys.hmacShaKeyFor(
                        SecurityConstants.JWT_SECRET.getBytes(StandardCharsets.UTF_8)),
                        SignatureAlgorithm.HS256)
                .compact();
    }

    public String getUsernameFromJWT(String token) {
        Claims claims = Jwts
                .parser()
                .setSigningKey(Keys.hmacShaKeyFor(SecurityConstants.JWT_SECRET.getBytes(StandardCharsets.UTF_8)))
                .build()
                .parseClaimsJws(token)
                .getBody();

        return claims.getSubject();
    }

    public boolean validadeToken(String token) {
        try {
            Jwts
                    .parser()
                    .setSigningKey(Keys.hmacShaKeyFor(SecurityConstants.JWT_SECRET.getBytes(StandardCharsets.UTF_8)))
                    .build()
                    .parseClaimsJws(token);
            return true;
        } catch (Exception ex) {
            throw new AuthenticationCredentialsNotFoundException("JWT expirou ou está incorreto", ex);
        }
    }
}