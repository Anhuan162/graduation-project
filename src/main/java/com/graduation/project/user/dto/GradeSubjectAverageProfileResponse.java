package com.graduation.project.user.dto;

import java.util.UUID;

import lombok.*;

@Getter
@Setter
@Data
@Builder
public class GradeSubjectAverageProfileResponse {
  UUID id;
  String letterCurrentScore;
  String letterImprovementScore;
  double currentScore;
  double improvementScore;
  String subjectName;
  int credit;
}
