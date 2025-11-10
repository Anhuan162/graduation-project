package com.graduation.project.user.dto;

import java.util.List;
import lombok.*;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class GpaProfileRequest {
  String id;
  String gpaProfileCode;
  Double letterGpaScore;
  Double numberGpaScore;
  Double previousNumberGpaScore;
  int passedCredits;
  List<GradeSubjectAverageProfileRequest> gradeSubjectAverageProfileRequests;
}
