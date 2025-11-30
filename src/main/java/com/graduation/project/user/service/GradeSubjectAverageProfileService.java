package com.graduation.project.user.service;

import com.graduation.project.auth.exception.AppException;
import com.graduation.project.auth.exception.ErrorCode;
import com.graduation.project.common.entity.*;
import com.graduation.project.common.repository.FacultyRepository;
import com.graduation.project.common.repository.GradeSubjectAverageProfileRepository;
import com.graduation.project.common.repository.SemesterRepository;
import com.graduation.project.common.repository.SubjectReferenceRepository;
import com.graduation.project.user.dto.GradeSubjectAverageProfileRequest;
import jakarta.transaction.Transactional;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.springframework.stereotype.Service;

@Transactional
@RequiredArgsConstructor
@Service
@Log4j2
public class GradeSubjectAverageProfileService {
  private final GradeSubjectAverageProfileRepository gradeSubjectAverageProfileRepository;
  private final SemesterRepository semesterRepository;
  private final FacultyRepository facultyRepository;
  private final SubjectReferenceRepository subjectReferenceRepository;

  public List<GradeSubjectAverageProfile> addGradeSubjectAverageProfileList(
      int currentSemesterId, String facultyCode, String cohortCode, GpaProfile gpaProfile) {
    Faculty faculty =
        facultyRepository
            .findByFacultyCode(facultyCode)
            .orElseThrow(() -> new AppException(ErrorCode.FACULTY_NOT_FOUND));

    Semester semester =
        semesterRepository
            .findById(currentSemesterId)
            .orElseThrow(() -> new AppException(ErrorCode.SEMESTER_NOT_FOUND));

    List<SubjectReference> subjectReferences =
        subjectReferenceRepository.findAllBySemesterAndFacultyAndCohortCode(
            semester, faculty, CohortCode.valueOf(cohortCode));

    List<GradeSubjectAverageProfile> gradeSubjectAverageProfiles = new ArrayList<>();
    subjectReferences.forEach(
        subjectReference -> {
          GradeSubjectAverageProfile gradeSubjectAverageProfile = new GradeSubjectAverageProfile();
          gradeSubjectAverageProfile.setSubjectReference(subjectReference);
          gradeSubjectAverageProfile.setGpaProfile(gpaProfile);
          gradeSubjectAverageProfiles.add(gradeSubjectAverageProfile);
        });

    return gradeSubjectAverageProfiles;
  }

  public GradeSubjectAverageProfile updateGradeAverageScoreProfile(
      GradeSubjectAverageProfileRequest gradeSubjectAverageProfileRequest) {
    GradeSubjectAverageProfile gradeSubjectAverageProfile =
        gradeSubjectAverageProfileRepository
            .findById(UUID.fromString(gradeSubjectAverageProfileRequest.getId()))
            .orElseThrow();
    gradeSubjectAverageProfile.setLetterCurrentScore(
        gradeSubjectAverageProfileRequest.getLetterCurrentScore());
    gradeSubjectAverageProfile.setLetterImprovementScore(
        gradeSubjectAverageProfileRequest.getLetterImprovementScore());
    gradeSubjectAverageProfile.setCurrentScore(
        Grade.toScore(gradeSubjectAverageProfileRequest.getLetterCurrentScore()));
    gradeSubjectAverageProfile.setImprovementScore(
        Grade.toScore(gradeSubjectAverageProfileRequest.getLetterImprovementScore()));
    return gradeSubjectAverageProfile;
  }
}
