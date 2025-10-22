package com.graduation.project.auth.service;

import com.graduation.project.auth.exception.AppException;
import com.graduation.project.auth.exception.ErrorCode;
import com.graduation.project.common.entity.User;
import com.graduation.project.common.repository.InvalidatedTokenRepository;
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

@Service
@Log4j2
public class TokenService {

  private final InvalidatedTokenRepository invalidatedTokenRepository;

  @Value("${app.jwt.secret}")
  protected String secretKey;

  @Value("${app.jwt.accessTokenExpirationMs}")
  protected long accessTokenValidityMs;

  @Value("${app.jwt.refreshTokenExpirationMs}")
  protected long refreshTokenValidityMs;

  public TokenService(InvalidatedTokenRepository invalidatedTokenRepository) {
    this.invalidatedTokenRepository = invalidatedTokenRepository;
  }

  public String generateToken(User user, boolean isRefreshToken) {
    JWSHeader header = new JWSHeader(JWSAlgorithm.HS512);

    JWTClaimsSet jwtClaimsSet =
        isRefreshToken
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
        .claim("scope", buildScope(user))
        .build();
  }

  public SignedJWT verifyToken(String token, boolean isRefresh)
      throws JOSEException, ParseException {
    JWSVerifier verifier = new MACVerifier(secretKey.getBytes());

    SignedJWT signedJWT = SignedJWT.parse(token);

    Date expiryTime =
        (isRefresh)
            ? new Date(
                signedJWT
                    .getJWTClaimsSet()
                    .getIssueTime()
                    .toInstant()
                    .plus(refreshTokenValidityMs, ChronoUnit.SECONDS)
                    .toEpochMilli())
            : signedJWT.getJWTClaimsSet().getExpirationTime();

    var verified = signedJWT.verify(verifier);

    if (!(verified && expiryTime.after(new Date())))
      throw new AppException(ErrorCode.UNAUTHENTICATED);

    if (invalidatedTokenRepository.existsById(UUID.fromString(signedJWT.getJWTClaimsSet().getJWTID())))
      throw new AppException(ErrorCode.UNAUTHENTICATED);

    return signedJWT;
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
