package com.graduation.project.announcement.dto;

import com.graduation.project.announcement.validation.ValidAnnouncementTarget;
import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@ValidAnnouncementTarget(message = "If announcement is not global, at least one target audience (Faculty, Cohort, or Class) must be specified.")
public class CreateAnnouncementRequest {

    @NotBlank(message = "Title is mandatory")
    private String title;

    @NotBlank(message = "Content is mandatory")
    private String content;

    private com.graduation.project.announcement.constant.AnnouncementType announcementType;

    private Boolean isGlobal;

    @Builder.Default
    private Set<String> targetFaculties = new HashSet<>();

    @Builder.Default
    private Set<String> targetCohorts = new HashSet<>();

    @Builder.Default
    private Set<String> specificClassCodes = new HashSet<>();

    private List<String> fileMetadataIds;
}
