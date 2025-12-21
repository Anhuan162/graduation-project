package com.graduation.project.auth.validation;

import jakarta.validation.Constraint;
import jakarta.validation.Payload;
import java.lang.annotation.*;

@Documented
@Retention(RetentionPolicy.RUNTIME)
@Target({ ElementType.FIELD, ElementType.PARAMETER })
@Constraint(validatedBy = FileSizeValidator.class)
public @interface FileSize {
    String message() default "File size exceeds the maximum allowed size";

    Class<?>[] groups() default {};

    Class<? extends Payload>[] payload() default {};

    long max() default 5242880; // 5MB default
}
