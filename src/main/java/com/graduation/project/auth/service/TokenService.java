package com.graduation.project.auth.service;

import com.graduation.project.auth.repository.InvalidatedTokenRepository;
import com.graduation.project.auth.repository.UserRepository;
import com.graduation.project.common.entity.User;
import com.graduation.project.security.exception.AppException;
import com.graduation.project.security.exception.ErrorCode;
import com.nimbusds.jose.*;
import com.nimbusds.jose.crypto.MACSigner;
import com.nimbusds.jose.crypto.MACVerifier;
import com.nimbusds.jwt.JWTClaimsSet;
import com.nimbusds.jwt.SignedJWT;
import com.nimbusds.oauth2.sdk.util.CollectionUtils;
import java.text.ParseException;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.Date;
import java.util.StringJoiner;
import java.util.UUID;
import lombok.extern.log4j.Log4j2;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

@Service
@Log4j2
public class TokenService {

  private final InvalidatedTokenRepository invalidatedTokenRepository;
  private final UserRepository userRepository;

  @Value("${app.jwt.secret}")
  protected String secretKey;

  @Value("${app.jwt.accessTokenExpirationMs}")
  protected long accessTokenValidityMs;

  @Value("${app.jwt.refreshTokenExpirationMs}")
  protected long refreshTokenValidityMs;

  public TokenService(
      InvalidatedTokenRepository invalidatedTokenRepository, UserRepository userRepository) {
    this.invalidatedTokenRepository = invalidatedTokenRepository;
    this.userRepository = userRepository;
  }

  @Transactional
  public String generateToken(User user, boolean isRefreshToken) {
    if (!isRefreshToken) {
      user = userRepository.findById(user.getId()).orElse(user);
    }
    JWSHeader header = new JWSHeader(JWSAlgorithm.HS512);

    JWTClaimsSet jwtClaimsSet = isRefreshToken
        ? buildJwtClaimSetForRefreshToken(user)
        : buildJwtClaimSetForAccessToken(user);

    Payload payload = new Payload(jwtClaimsSet.toJSONObject());

    JWSObject jwsObject = new JWSObject(header, payload);

    try {
      jwsObject.sign(new MACSigner(secretKey.getBytes()));
      return jwsObject.serialize();
    } catch (JOSEException e) {
      log.error("Cannot create token", e);
      throw new RuntimeException(e);
    }
  }

  private JWTClaimsSet buildJwtClaimSetForRefreshToken(User user) {
    return new JWTClaimsSet.Builder()
        .subject(user.getEmail())
        .issuer("graduation_project.com")
        .issueTime(new Date())
        .expirationTime(
            new Date(Instant.now().plus(refreshTokenValidityMs, ChronoUnit.SECONDS).toEpochMilli()))
        .jwtID(UUID.randomUUID().toString())
        .claim("userId", user.getId())
        .build();
  }

  private JWTClaimsSet buildJwtClaimSetForAccessToken(User user) {
    return new JWTClaimsSet.Builder()
        .subject(user.getEmail())
        .issuer("graduation_project.com")
        .issueTime(new Date())
        .expirationTime(
            new Date(Instant.now().plus(accessTokenValidityMs, ChronoUnit.SECONDS).toEpochMilli()))
        .jwtID(UUID.randomUUID().toString())
        .claim("userId", user.getId())
        .claim("scope", buildScope(user))
        .claim("userId", user.getId().toString())
        .claim("fullName", user.getFullName())
        .claim("avatar", user.getAvatarUrl())
        .build();
  }

  public SignedJWT verifyToken(String token, boolean isRefresh) {
    if (token == null || token.isBlank()) {
      throw new AppException(ErrorCode.UNAUTHENTICATED);
    }

    try {
      JWSVerifier verifier = new MACVerifier(secretKey.getBytes());
      SignedJWT signedJWT = SignedJWT.parse(token);

      // 1. Verify Signature
      if (!signedJWT.verify(verifier)) {
        log.error("Token verification failed: Signature invalid");
        throw new AppException(ErrorCode.UNAUTHENTICATED);
      }

      JWTClaimsSet claims = signedJWT.getJWTClaimsSet();

      // 2. Verify Expiration
      if (new Date().after(claims.getExpirationTime())) {
        log.error("Token verification failed: Token expired. Exp: {}", claims.getExpirationTime());
        throw new AppException(ErrorCode.UNAUTHENTICATED);
      }

      // 3. Verify JIT (Invalidated Token)
      if (invalidatedTokenRepository.existsByJit(claims.getJWTID())) {
        log.error("Token verification failed: Token JIT invalidated. JIT: {}", claims.getJWTID());
        throw new AppException(ErrorCode.UNAUTHENTICATED);
      }

      // 4. Verify User Status & Invalidation Timestamp
      String email = claims.getSubject();
      User user = userRepository.findByEmail(email)
          .orElseThrow(() -> {
            log.error("Token verification failed: User not found with email {}", email);
            return new AppException(ErrorCode.UNAUTHENTICATED);
          });

      return signedJWT;
    } catch (ParseException | JOSEException e) {
      log.error("Token verification failed: {}", e.getMessage());
      throw new AppException(ErrorCode.UNAUTHENTICATED);
    }
  }

  @Transactional(propagation = Propagation.REQUIRES_NEW)
  public void revokeAllUserTokens(User user) {
    // user.setTokenInvalidationTimestamp(new Date());
    // userRepository.save(user);
  }

  private String buildScope(User user) {
    StringJoiner stringJoiner = new StringJoiner(" ");
    if (CollectionUtils.isNotEmpty(user.getRoles())) {
      user.getRoles()
          .forEach(
              role -> {
                stringJoiner.add("ROLE_" + role.getName());
                if (!CollectionUtils.isEmpty(role.getPermissions())) {
                  role.getPermissions()
                      .forEach(permission -> stringJoiner.add(permission.getName()));
                }
              });
    }

    return stringJoiner.toString();
  }
}
