package com.graduation.project.announcement.dto;

import com.graduation.project.cpa.constant.CohortCode;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Builder
public class ClassroomResponse {
  private String id;
  private String className;
  private String classCode;
  private String facultyName;
  private int startedYear;
  private int endedYear;
  private CohortCode schoolYearCode;
}
