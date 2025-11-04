package com.graduation.project.user.dto;

import java.util.List;
import java.util.UUID;
import lombok.Builder;
import lombok.Value;

@Value
@Builder
public class GpaProfileResponse {
  UUID id;
  String gpaProfileCode;
  double letterGpaScore;
  double numberGpaScore;
  double previousNumberGpaScore;
  int passedCredits;
  List<GradeSubjectAverageProfileResponse> gradeSubjectAverageProfileResponses;
}
