package com.graduation.project.announcement.dto;

import com.graduation.project.cpa.constant.CohortCode;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Builder
public class FilterClassroomResponse {
  private String className;
  private String classCode;
}
