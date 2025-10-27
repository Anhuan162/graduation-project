package com.graduation.project.common.validation;

import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;

public class StrongPasswordValidator implements ConstraintValidator<StrongPassword, String> {
  private static final String STRONG_PASSWORD_REGEX =
      "^(?=.*[a-z])(?=.*[A-Z])(?=.*\\d)(?=.*[@$!%*?&])[A-Za-z\\d@$!%*?&]{8,32}$";

  @Override
  public boolean isValid(String password, ConstraintValidatorContext constraintValidatorContext) {
      if (password == null) {
          return false;
      }
      if (password.contains(" ")) {
          return false;
      }
      return password.matches(STRONG_PASSWORD_REGEX);
  }

  @Override
  public void initialize(StrongPassword constraintAnnotation) {
    ConstraintValidator.super.initialize(constraintAnnotation);
  }
}
