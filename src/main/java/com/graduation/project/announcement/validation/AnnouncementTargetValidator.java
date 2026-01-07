package com.graduation.project.announcement.validation;

import com.graduation.project.announcement.dto.CreateAnnouncementRequest;
import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;
import org.springframework.util.CollectionUtils;

import java.util.Objects;

public class AnnouncementTargetValidator
        implements ConstraintValidator<ValidAnnouncementTarget, CreateAnnouncementRequest> {

    @Override
    public boolean isValid(CreateAnnouncementRequest request, ConstraintValidatorContext context) {
        if (request == null) {
            return true; // Let @NotNull handle null request if needed, or return false here.
        }

        // If isGlobal is true, targeting is valid (covers everyone)
        if (Boolean.TRUE.equals(request.getIsGlobal())) {
            return true;
        }

        // If isGlobal is false (or null), we must checks specific targets
        boolean hasFaculties = !CollectionUtils.isEmpty(request.getTargetFaculties());
        boolean hasCohorts = !CollectionUtils.isEmpty(request.getTargetCohorts());
        boolean hasSpecificClasses = !CollectionUtils.isEmpty(request.getSpecificClassCodes());

        // At least one target list must be non-empty
        return hasFaculties || hasCohorts || hasSpecificClasses;
    }
}
