package com.graduation.project.cpa.service;

import com.graduation.project.announcement.entity.Faculty;
import com.graduation.project.security.exception.AppException;
import com.graduation.project.security.exception.ErrorCode;
import com.graduation.project.cpa.constant.CohortCode;
import com.graduation.project.cpa.constant.Grade;
import com.graduation.project.announcement.repository.FacultyRepository;
import com.graduation.project.cpa.entity.GpaProfile;
import com.graduation.project.cpa.entity.GradeSubjectAverageProfile;
import com.graduation.project.cpa.repository.GradeSubjectAverageProfileRepository;
import com.graduation.project.library.entity.Semester;
import com.graduation.project.library.repository.SemesterRepository;
import com.graduation.project.library.entity.SubjectReference;
import com.graduation.project.library.repository.SubjectReferenceRepository;
import com.graduation.project.cpa.dto.GradeSubjectAverageProfileRequest;
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
