package com.graduation.project.announcement.dto;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class CreatedFacultyRequest {
  private String facultyName;
  private String facultyCode;
  private String description;
}
