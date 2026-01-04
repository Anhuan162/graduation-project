package com.graduation.project.security.ultilities;

import com.graduation.project.auth.security.UserPrincipal;
import java.util.Collection;
import java.util.UUID;
import org.springframework.core.convert.converter.Converter;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.server.resource.authentication.JwtGrantedAuthoritiesConverter;

public class JwtToUserPrincipalConverter
    implements Converter<Jwt, UsernamePasswordAuthenticationToken> {

  private final JwtGrantedAuthoritiesConverter authoritiesConverter;

  public JwtToUserPrincipalConverter() {
    this.authoritiesConverter = new JwtGrantedAuthoritiesConverter();

    this.authoritiesConverter.setAuthoritiesClaimName("scope");

    this.authoritiesConverter.setAuthorityPrefix("");
  }

  @Override
  public UsernamePasswordAuthenticationToken convert(Jwt jwt) {
    // Lấy danh sách quyền từ JWT
    Collection<GrantedAuthority> authorities = authoritiesConverter.convert(jwt);

    String email = jwt.getSubject();
    String userIdStr = jwt.getClaimAsString("userId");

    if (userIdStr == null || userIdStr.isBlank()) {
      throw new BadCredentialsException("Missing User ID in Token");
    }

    final UUID id;
    try {
      id = UUID.fromString(userIdStr);
    } catch (IllegalArgumentException e) {
      throw new BadCredentialsException("Invalid User ID in Token", e);
    }

    String fullName = jwt.getClaimAsString("fullName");
    String avatar = jwt.getClaimAsString("avatar");

    UserPrincipal principal = new UserPrincipal(
        id,
        email,
        "",
        fullName == null ? "" : fullName,
        avatar == null ? "" : avatar,
        authorities);

    return new UsernamePasswordAuthenticationToken(principal, jwt, authorities);
  }
}
