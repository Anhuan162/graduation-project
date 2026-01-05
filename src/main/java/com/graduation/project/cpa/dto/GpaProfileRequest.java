package com.graduation.project.cpa.dto;

import java.util.List;
import lombok.*;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class GpaProfileRequest {
  String id;
  String gpaProfileCode;
  String letterGpaScore;
  Double numberGpaScore;
  Double previousNumberGpaScore;
  int passedCredits;
  List<GradeSubjectAverageProfileRequest> gradeSubjectAverageProfileRequests;
}
