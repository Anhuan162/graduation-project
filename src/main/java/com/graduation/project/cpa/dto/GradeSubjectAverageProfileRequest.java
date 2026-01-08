package com.graduation.project.cpa.dto;

import lombok.*;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class GradeSubjectAverageProfileRequest {
  String id;
  /**
   * ID of the Subject (Master Data).
   * Backend will automatically link to the correct SubjectReference based on
   * context.
   */
  String subjectId;
  String letterCurrentScore;
  String letterImprovementScore;
}
