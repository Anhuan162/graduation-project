package com.graduation.project.auth.dto.response;

import com.graduation.project.common.constant.Provider;
import com.graduation.project.common.entity.User;
import java.util.UUID;
import lombok.Builder;
import lombok.Value;

@Value
@Builder
public class SignupResponse {
  UUID id;
  String email;
  Provider provider;

  public static SignupResponse from(User user) {
    return SignupResponse.builder()
        .id(user.getId())
        .email(user.getEmail())
        .provider(user.getProvider())
        .build();
  }
}
