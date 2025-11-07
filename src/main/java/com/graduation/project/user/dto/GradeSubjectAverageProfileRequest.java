package com.graduation.project.user.dto;

import lombok.Builder;
import lombok.Value;

@Value
@Builder
public class GradeSubjectAverageProfileRequest {
  String gradeSubjectAverageProfileId;
  String letterCurrentScore;
  String letterImprovementScore;
  String currentScore;
  String improvementScore;
}
