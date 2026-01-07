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
  private String subjectName;
  private String facultyId;
  private String facultyName;
  private Integer semesterId;
  private String cohortCode;

  public static SubjectReferenceResponse toSubjectReferenceResponse(
      SubjectReference subjectReference) {
    return SubjectReferenceResponse.builder()
        .subjectReferenceId(String.valueOf(subjectReference.getId()))
        .subjectName(subjectReference.getSubject().getSubjectName())
        .facultyId(String.valueOf(subjectReference.getFaculty().getId()))
        .facultyName(subjectReference.getFaculty().getFacultyName())
        .semesterId(subjectReference.getSemester().getId())
        .subjectId(String.valueOf(subjectReference.getSubject().getId()))
        .cohortCode(String.valueOf(subjectReference.getCohortCode()))
        .build();
  }
}
