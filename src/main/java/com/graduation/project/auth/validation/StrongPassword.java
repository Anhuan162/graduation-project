package com.graduation.project.auth.validation;

import jakarta.validation.Constraint;
import jakarta.validation.Payload;
import java.lang.annotation.*;

@Documented
@Retention(RetentionPolicy.RUNTIME)
@Target({ElementType.FIELD, ElementType.PARAMETER})
@Constraint(validatedBy = StrongPasswordValidator.class)
public @interface StrongPassword {
  String message() default
      "Password must be 8–32 chars, include upper, lower, digit, and special character, and contain no spaces.";

  Class<?>[] groups() default {};

  Class<? extends Payload>[] payload() default {};
}
