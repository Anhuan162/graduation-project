package com.graduation.project.security.config;

import com.graduation.project.auth.dto.request.IntrospectRequest;
import com.graduation.project.auth.service.AuthService;
import com.nimbusds.jose.JOSEException;
import java.text.ParseException;
import java.util.Objects;
import javax.crypto.spec.SecretKeySpec;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.oauth2.jose.jws.MacAlgorithm;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.security.oauth2.jwt.JwtException;
import org.springframework.security.oauth2.jwt.NimbusJwtDecoder;
import org.springframework.stereotype.Component;

@Component
public class CustomJwtDecoder implements JwtDecoder {
  @Value("${app.jwt.secret}")
  private String signerKey;

  private final AuthService authService;

  private NimbusJwtDecoder nimbusJwtDecoder = null;

  public CustomJwtDecoder(AuthService authService) {
    this.authService = authService;
  }

  @Override
  public Jwt decode(String token) throws JwtException {

    try {
      var response = authService.introspect(IntrospectRequest.builder().token(token).build());

      if (!response.isValid()) throw new JwtException("Token invalid");
    } catch (JOSEException | ParseException e) {
      throw new JwtException(e.getMessage());
    }

    if (Objects.isNull(nimbusJwtDecoder)) {
      SecretKeySpec secretKeySpec = new SecretKeySpec(signerKey.getBytes(), "HS512");
      nimbusJwtDecoder =
          NimbusJwtDecoder.withSecretKey(secretKeySpec).macAlgorithm(MacAlgorithm.HS512).build();
    }

    return nimbusJwtDecoder.decode(token);
  }
}
