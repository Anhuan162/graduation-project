package com.graduation.project.user.dto;

import java.util.List;
import java.util.UUID;
import lombok.Builder;
import lombok.Data;
import lombok.Value;

@Data
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
