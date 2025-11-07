package com.graduation.project.user.dto;

import java.util.List;
import lombok.Builder;
import lombok.Value;

@Value
@Builder
public class GpaProfileRequest {
  String gpaProfileId;
  String gpaProfileCode;
  double letterGpaScore;
  double numberGpaScore;
  double previousNumberGpaScore;
  int passedCredits;
  List<GradeSubjectAverageProfileRequest> gradeSubjectAverageProfileRequests;
}
