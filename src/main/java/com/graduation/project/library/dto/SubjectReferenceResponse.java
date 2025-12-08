package com.graduation.project.library.dto;

import com.graduation.project.library.entity.SubjectReference;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SubjectReferenceResponse {
  private String subjectReferenceId;
  private String subjectId;
  private String facultyId;
  private Integer semesterId;
  private String cohortCode;

  public static SubjectReferenceResponse toSubjectReferenceResponse(
      SubjectReference subjectReference) {
    return SubjectReferenceResponse.builder()
        .subjectReferenceId(String.valueOf(subjectReference.getId()))
        .facultyId(String.valueOf(subjectReference.getFaculty().getId()))
        .semesterId(subjectReference.getSemester().getId())
        .subjectId(String.valueOf(subjectReference.getSubject().getId()))
        .cohortCode(String.valueOf(subjectReference.getCohortCode()))
        .build();
  }
}
