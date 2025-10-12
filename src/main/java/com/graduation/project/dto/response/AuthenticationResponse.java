package com.graduation.project.dto.response;

import com.graduation.project.entity.Provider;
import com.graduation.project.entity.User;
import java.util.UUID;
import lombok.Builder;
import lombok.Value;

@Value
@Builder
public class AuthenticationResponse {
  UUID id;
  String email;
  String password;
  Provider provider;

  public static AuthenticationResponse from(User user) {
    return AuthenticationResponse.builder()
        .id(user.getId())
        .email(user.getEmail())
        .password(user.getPassword())
        .provider(user.getProvider())
        .build();
  }
}
