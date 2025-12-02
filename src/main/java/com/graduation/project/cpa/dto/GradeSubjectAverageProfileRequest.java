package com.graduation.project.cpa.dto;

import lombok.*;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class GradeSubjectAverageProfileRequest {
  String id;
  String letterCurrentScore;
  String letterImprovementScore;
}
