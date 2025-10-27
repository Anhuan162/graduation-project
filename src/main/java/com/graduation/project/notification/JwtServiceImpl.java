package com.graduation.project.notification;

import com.graduation.project.auth.config.CustomJwtDecoder;
import com.graduation.project.auth.repository.UserRepository;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.stereotype.Service;

// Example: call your AuthService.introspect or decode token to extract user id claim
@Service
public class JwtServiceImpl implements JwtService {

  private final CustomJwtDecoder jwtDecoder;
  private final UserRepository userRepository;

  public JwtServiceImpl(CustomJwtDecoder jwtDecoder, UserRepository userRepository) {
    this.jwtDecoder = jwtDecoder;
    this.userRepository = userRepository;
  }

  @Override
  public String getUserIdFromToken(String token) {
    try {
      Jwt jwt = jwtDecoder.decode(token);
      String email = jwt.getSubject();

      var user =
          userRepository
              .findByEmail(email)
              .orElseThrow(() -> new RuntimeException("User not found"));

      return user.getId().toString();
    } catch (Exception e) {
      throw new RuntimeException("Invalid token", e);
    }
  }
}
