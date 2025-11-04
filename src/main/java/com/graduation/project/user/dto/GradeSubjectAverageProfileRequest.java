package com.graduation.project.user.dto;

import lombok.Builder;
import lombok.Value;

@Value
@Builder
public class GradeSubjectAverageProfileRequest {
  String letterCurrentScore;
  String letterImprovementScore;
  double currentScore;
  double improvementScore;
}
