package com.graduation.project.cpa.dto;

import lombok.Builder;
import lombok.Value;

@Builder
@Value
public class CreatedGradeSubjectAverageProfileRequest {
  String letterCurrentScore;
  String letterImprovementScore;
  double currentScore;
  double improvementScore;
}
