package com.graduation.project.announcement.dto;

import com.graduation.project.cpa.constant.CohortCode;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class UpdatedClassroomRequest {
  private String className;
  private int startedYear;
  private int endedYear;
  private CohortCode schoolYearCode;
}
