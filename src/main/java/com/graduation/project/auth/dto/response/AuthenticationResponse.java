package com.graduation.project.auth.dto.response;

import java.util.List;
import lombok.*;
import lombok.experimental.FieldDefaults;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@FieldDefaults(level = AccessLevel.PRIVATE)
public class AuthenticationResponse {
  private UserResponse userResponse;
  private List<PermissionResponse> permissionResponse;
  private TokenResponse tokenResponse;
}
