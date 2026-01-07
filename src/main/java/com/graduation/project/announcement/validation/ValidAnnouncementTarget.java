package com.graduation.project.announcement.validation;

import jakarta.validation.Constraint;
import jakarta.validation.Payload;
import java.lang.annotation.*;

@Documented
@Constraint(validatedBy = AnnouncementTargetValidator.class)
@Target({ ElementType.TYPE })
@Retention(RetentionPolicy.RUNTIME)
public @interface ValidAnnouncementTarget {
    String message() default "Invalid announcement targeting";

    Class<?>[] groups() default {};

    Class<? extends Payload>[] payload() default {};
}
