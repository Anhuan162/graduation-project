package com.graduation.project.user.service;

import com.graduation.project.auth.exception.AppException;
import com.graduation.project.auth.exception.ErrorCode;
import com.graduation.project.common.entity.*;
import com.graduation.project.common.repository.FacultyRepository;
import com.graduation.project.common.repository.GradeSubjectAverageProfileRepository;
import com.graduation.project.common.repository.SemesterRepository;
import com.graduation.project.common.repository.SubjectReferenceRepository;
import com.graduation.project.user.dto.GradeSubjectAverageProfileResponse;
import com.graduation.project.user.mapper.GradeSubjectAverageProfileMapper;
import java.util.ArrayList;
import java.util.List;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.springframework.stereotype.Service;

@RequiredArgsConstructor
@Service
@Log4j2
public class GradeSubjectAverageProfileService {
  private final GradeSubjectAverageProfileRepository gradeSubjectAverageProfileRepository;
  private final SemesterRepository semesterRepository;
  private final FacultyRepository facultyRepository;
  private final SubjectReferenceRepository subjectReferenceRepository;
  private final GradeSubjectAverageProfileMapper gradeSubjectAverageProfileMapper;

  public List<GradeSubjectAverageProfileResponse> findAllSubjectsBySemesterAndFacultyAndCohortCode(
      int i, String facultyCode, String cohortCode) {
    Faculty faculty = facultyRepository.findByFacultyCode(facultyCode);

    Semester semester =
        semesterRepository
            .findById(i)
            .orElseThrow(() -> new AppException(ErrorCode.SEMESTER_NOT_FOUND));

    List<SubjectReference> subjectReferences =
        subjectReferenceRepository.findAllBySemesterAndFacultyAndCohortCode(
            semester, faculty, CohortCode.valueOf(cohortCode));

    List<GradeSubjectAverageProfileResponse> gradeSubjectAverageProfileResponses =
        new ArrayList<>();
    List<GradeSubjectAverageProfile> gradeSubjectAverageProfiles = new ArrayList<>();
    subjectReferences.forEach(
        subjectReference -> {
          GradeSubjectAverageProfile gradeSubjectAverageProfile = new GradeSubjectAverageProfile();
          gradeSubjectAverageProfile.setSubjectReference(subjectReference);
          gradeSubjectAverageProfiles.add(gradeSubjectAverageProfile);
        });
    gradeSubjectAverageProfileRepository.saveAll(gradeSubjectAverageProfiles);

    gradeSubjectAverageProfiles.forEach(
        gradeSubjectAverageProfile -> {
          GradeSubjectAverageProfileResponse response =
              gradeSubjectAverageProfileMapper.toGradeSubjectAverageProfileResponse(
                  gradeSubjectAverageProfile);
          response.setCredit(
              gradeSubjectAverageProfile.getSubjectReference().getSubject().getCredit());
          response.setSubjectName(
              gradeSubjectAverageProfile.getSubjectReference().getSubject().getSubjectName());

          gradeSubjectAverageProfileResponses.add(response);
        });
    return gradeSubjectAverageProfileResponses;
  }
}
