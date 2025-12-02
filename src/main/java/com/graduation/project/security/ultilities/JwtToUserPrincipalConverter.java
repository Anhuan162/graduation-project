package com.graduation.project.security.ultilities;

import java.util.Collection;
import java.util.UUID;

import com.graduation.project.auth.security.UserPrincipal;
import org.springframework.core.convert.converter.Converter;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.server.resource.authentication.JwtGrantedAuthoritiesConverter;

public class JwtToUserPrincipalConverter
    implements Converter<Jwt, UsernamePasswordAuthenticationToken> {

  private final JwtGrantedAuthoritiesConverter authoritiesConverter;

  public JwtToUserPrincipalConverter() {
    this.authoritiesConverter = new JwtGrantedAuthoritiesConverter();
    this.authoritiesConverter.setAuthorityPrefix(""); // thêm prefix để Spring hiểu là role
  }

  @Override
  public UsernamePasswordAuthenticationToken convert(Jwt jwt) {
    // Lấy danh sách quyền từ JWT
    Collection<GrantedAuthority> authorities = authoritiesConverter.convert(jwt);

    // Lấy các thông tin cơ bản của user từ claim
    String email = jwt.getSubject();
    String idStr = jwt.getId();

    UUID id = null;
    try {
      if (idStr != null) id = UUID.fromString(idStr);
    } catch (Exception ignored) {
    }

    // Tạo principal (UserPrincipal)
    UserPrincipal principal = new UserPrincipal(id, email, "", authorities);

    // Tạo authentication token (được Spring Security quản lý)
    return new UsernamePasswordAuthenticationToken(principal, jwt, authorities);
  }
}
