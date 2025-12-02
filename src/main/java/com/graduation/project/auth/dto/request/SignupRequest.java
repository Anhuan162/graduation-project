package com.graduation.project.auth.dto.request;

import com.graduation.project.auth.validation.PasswordMatches;
import com.graduation.project.auth.validation.StrongPassword;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import lombok.*;
import lombok.experimental.FieldDefaults;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@FieldDefaults(level = AccessLevel.PRIVATE)
@PasswordMatches
public class SignupRequest {
  @Email(message = "Invalid email format")
  @NotBlank(message = "Email is required")
  private String email;

  @StrongPassword private String password;

  @NotBlank(message = "Confirm password is required")
  private String confirmPassword;
}
