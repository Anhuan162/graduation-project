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
  Double currentScore;
  Double improvementScore;
  String subjectName;
  String subjectCode;
  UUID subjectId;
  int credit;
}
