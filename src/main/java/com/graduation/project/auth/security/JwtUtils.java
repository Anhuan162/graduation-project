package com.graduation.project.auth.security;

import io.jsonwebtoken.*;
import io.jsonwebtoken.security.Keys;
import java.security.Key;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

@Component
public class JwtUtils {

  private final Key signingKey;
  private final long accessTokenValidityMs;
  private final long refreshTokenValidityMs;

  public JwtUtils(
      @Value("${app.jwt.secret}") String secret,
      @Value("${app.jwt.accessTokenExpirationMs}") long accessTokenValidityMs,
      @Value("${app.jwt.refreshTokenExpirationMs}") long refreshTokenValidityMs) {

    this.signingKey = Keys.hmacShaKeyFor(secret.getBytes());
    this.accessTokenValidityMs = accessTokenValidityMs;
    this.refreshTokenValidityMs = refreshTokenValidityMs;
  }

  public String generateAccessToken(String subject, UUID userId, List<String> roles) {
    Date now = new Date();
    Date expiry = new Date(now.getTime() + accessTokenValidityMs);

    return Jwts.builder()
        .setSubject(subject)
        .setIssuedAt(now)
        .setExpiration(expiry)
        .addClaims(Map.of("uid", userId, "roles", roles))
        .signWith(signingKey, SignatureAlgorithm.HS256)
        .compact();
  }

  public String generateRefreshToken(String subject) {
    Date now = new Date();
    Date expiry = new Date(now.getTime() + refreshTokenValidityMs);

    return Jwts.builder()
        .setSubject(subject)
        .setIssuedAt(now)
        .setExpiration(expiry)
        .signWith(signingKey, SignatureAlgorithm.HS256)
        .compact();
  }

  public boolean validate(String token) {
    try {
      Jwts.parserBuilder().setSigningKey(signingKey).build().parseClaimsJws(token);
      return true;
    } catch (JwtException e) {
      return false;
    }
  }

  public Claims getClaims(String token) {
    return Jwts.parserBuilder().setSigningKey(signingKey).build().parseClaimsJws(token).getBody();
  }

  public String getUsername(String token) {
    return getClaims(token).getSubject();
  }
}
