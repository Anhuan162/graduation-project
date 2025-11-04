package com.graduation.project.user.service;

import com.graduation.project.common.entity.*;
import com.graduation.project.common.repository.GpaProfileRepository;
import com.graduation.project.user.dto.GradeSubjectAverageProfileResponse;
import java.util.List;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.springframework.stereotype.Service;

@RequiredArgsConstructor
@Service
@Log4j2
public class GpaProfileService {
  private final GpaProfileRepository gpaProfileRepository;

  private final GradeSubjectAverageProfileService gradeSubjectAverageProfileService;

  public GpaProfile addGpaProfile(String cpaProfileCode, int i) {
    String gpaProfileCode = cpaProfileCode + i;
    String cohortCode = "D" + cpaProfileCode.substring(4, 6);
    String facultyCode = cpaProfileCode.substring(8, 10);

    List<GradeSubjectAverageProfileResponse> gradeSubjectAverageProfiles =
        gradeSubjectAverageProfileService.findAllSubjectsBySemesterAndFacultyAndCohortCode(
            i, facultyCode, cohortCode);

    return null;
  }
}
