package com.graduation.project.common.validation;

import jakarta.validation.Constraint;
import jakarta.validation.Payload;
import java.lang.annotation.*;

@Documented
@Constraint(validatedBy = PasswordMatchesValidator.class)
@Target(ElementType.TYPE) // Áp dụng ở cấp class
@Retention(RetentionPolicy.RUNTIME)
public @interface PasswordMatches {
  String message() default "Password and Confirm Password do not match.";

  Class<?>[] groups() default {};

  Class<? extends Payload>[] payload() default {};
}
