package com.graduation.project.announcement.dto;

import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Builder
public class FacultyResponse {
    private String id;
    private String facultyName;
    private String facultyCode;
    private String description;
}
