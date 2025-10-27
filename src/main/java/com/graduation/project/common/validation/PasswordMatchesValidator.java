package com.graduation.project.common.validation;

import com.graduation.project.auth.dto.request.SignupRequest;
import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;

public class PasswordMatchesValidator
    implements ConstraintValidator<PasswordMatches, SignupRequest> {

  @Override
  public boolean isValid(SignupRequest request, ConstraintValidatorContext context) {
    if (request.getPassword() == null || request.getConfirmPassword() == null) {
      return false;
    }
    return request.getPassword().equals(request.getConfirmPassword());
  }
}
