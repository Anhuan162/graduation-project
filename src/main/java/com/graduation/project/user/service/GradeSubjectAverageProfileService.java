package com.graduation.project.user.service;

import com.graduation.project.auth.exception.AppException;
import com.graduation.project.auth.exception.ErrorCode;
import com.graduation.project.common.entity.*;
import com.graduation.project.common.repository.FacultyRepository;
import com.graduation.project.common.repository.GradeSubjectAverageProfileRepository;
import com.graduation.project.common.repository.SemesterRepository;
import com.graduation.project.common.repository.SubjectReferenceRepository;
import com.graduation.project.user.dto.GradeSubjectAverageProfileRequest;
import com.graduation.project.user.mapper.GradeSubjectAverageProfileMapper;
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
  private final GradeSubjectAverageProfileMapper gradeSubjectAverageProfileMapper;

  public List<GradeSubjectAverageProfile> addGradeSubjectAverageProfileList(
      int previousSemesterId, String facultyCode, String cohortCode) {
    Faculty faculty =
        facultyRepository
            .findByFacultyCode(facultyCode)
            .orElseThrow(() -> new AppException(ErrorCode.FACULTY_NOT_FOUND));

    Semester semester =
        semesterRepository
            .findById(previousSemesterId)
            .orElseThrow(() -> new AppException(ErrorCode.SEMESTER_NOT_FOUND));

    List<SubjectReference> subjectReferences =
        subjectReferenceRepository.findAllBySemesterAndFacultyAndCohortCode(
            semester, faculty, CohortCode.valueOf(cohortCode));

    List<GradeSubjectAverageProfile> gradeSubjectAverageProfiles = new ArrayList<>();
    subjectReferences.forEach(
        subjectReference -> {
          GradeSubjectAverageProfile gradeSubjectAverageProfile = new GradeSubjectAverageProfile();
          gradeSubjectAverageProfile.setSubjectReference(subjectReference);
          gradeSubjectAverageProfiles.add(gradeSubjectAverageProfile);
        });

    gradeSubjectAverageProfileRepository.saveAll(gradeSubjectAverageProfiles);
    return gradeSubjectAverageProfiles;
  }

  public GradeSubjectAverageProfile updateGradeAverageScoreProfile(
      GradeSubjectAverageProfileRequest gradeSubjectAverageProfileRequest) {
    GradeSubjectAverageProfile gradeSubjectAverageProfile =
        gradeSubjectAverageProfileRepository
            .findById(
                UUID.fromString(
                    gradeSubjectAverageProfileRequest.getGradeSubjectAverageProfileId()))
            .orElseThrow();
    gradeSubjectAverageProfile.setCurrentScore(
        Double.parseDouble(gradeSubjectAverageProfileRequest.getCurrentScore()));
    gradeSubjectAverageProfile.setImprovementScore(
        Double.parseDouble(gradeSubjectAverageProfileRequest.getImprovementScore()));
    return gradeSubjectAverageProfile;
  }
}
