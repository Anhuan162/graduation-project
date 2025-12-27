package com.graduation.project.auth.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class ChangePasswordRequest {
  @NotBlank(message = "passwordSessionId can not be blank")
  private String passwordSessionId;

  @NotBlank(message = "password can not be blank")
  @Size(min = 8, message = "password must be at least 8 characters")
  @Pattern(
      regexp = "^(?=.*[a-z])(?=.*[A-Z])(?=.*\\d)(?=.*[^A-Za-z0-9]).{8,}$",
      message =
          "Password must contain at least 1 uppercase, 1 lowercase, 1 number and 1 special character")
  private String newPassword;
}
