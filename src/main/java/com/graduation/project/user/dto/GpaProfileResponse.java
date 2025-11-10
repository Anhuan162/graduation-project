package com.graduation.project.user.dto;

import java.util.List;
import java.util.UUID;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class GpaProfileResponse {
  UUID id;
  String gpaProfileCode;
  String letterGpaScore;
  Double numberGpaScore;
  Double previousNumberGpaScore;
  int passedCredits;
  List<GradeSubjectAverageProfileResponse> gradeSubjectAverageProfileResponses;
}
