package com.graduation.project.cpa.service;

import com.graduation.project.security.exception.AppException;
import com.graduation.project.security.exception.ErrorCode;
import com.graduation.project.cpa.constant.Grade;
import com.graduation.project.cpa.entity.CpaProfile;
import com.graduation.project.cpa.entity.GpaProfile;
import com.graduation.project.cpa.entity.GradeSubjectAverageProfile;
import com.graduation.project.cpa.repository.GpaProfileRepository;
import com.graduation.project.cpa.dto.GpaProfileRequest;
import com.graduation.project.cpa.dto.GradeSubjectAverageProfileRequest;
import jakarta.transaction.Transactional;
import java.util.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.springframework.stereotype.Service;

@Transactional
@RequiredArgsConstructor
@Service
@Log4j2
public class GpaProfileService {
  private final GpaProfileRepository gpaProfileRepository;

  private final GradeSubjectAverageProfileService gradeSubjectAverageProfileService;
  private final com.graduation.project.library.repository.SemesterRepository semesterRepository;

  public GpaProfile addGpaProfile(String studentCode, int semesterId, CpaProfile cpaProfile) {
    String gpaProfileCode = "GPA" + studentCode + semesterId;
    // String cohortCode = "D" + studentCode.substring(1, 3);
    // String facultyCode = studentCode.substring(5, 7);

    com.graduation.project.library.entity.Semester semester = semesterRepository.findById(semesterId)
        .orElseThrow(() -> new AppException(ErrorCode.SEMESTER_NOT_FOUND));

    GpaProfile gpaProfile = GpaProfile.builder()
        .gpaProfileCode(gpaProfileCode)
        .semester(semester)
        .build();

    // Don't create default Subject Scores list
    // List<GradeSubjectAverageProfile> gradeSubjectAverageProfiles =
    // gradeSubjectAverageProfileService
    // .addGradeSubjectAverageProfileList(
    // semesterId, facultyCode, cohortCode, gpaProfile);
    // gpaProfile.setGradeSubjectAverageProfiles(gradeSubjectAverageProfiles);
    gpaProfile.setGradeSubjectAverageProfiles(new ArrayList<>()); // Initialize empty list
    gpaProfile.setCpaProfile(cpaProfile);
    return gpaProfile;
  }

  public GpaProfile updateGpaProfile(GpaProfileRequest gpaProfileRequest) {
    GpaProfile gpaProfile = gpaProfileRepository
        .findById(UUID.fromString(gpaProfileRequest.getId()))
        .orElseThrow(() -> new AppException(ErrorCode.GPA_PROFILE_NOT_FOUND));

    // 1. Process Updates from Request
    for (GradeSubjectAverageProfileRequest itemRequest : gpaProfileRequest
        .getGradeSubjectAverageProfileRequests()) {
      if (itemRequest.getId() == null) {
        // New Subject
        GradeSubjectAverageProfile score = gradeSubjectAverageProfileService
            .updateGradeAverageScoreProfile(itemRequest, gpaProfile);
        score.setGpaProfile(gpaProfile);
        if (!gpaProfile.getGradeSubjectAverageProfiles().contains(score)) {
          gpaProfile.getGradeSubjectAverageProfiles().add(score);
        }
      } else {
        // Existing Subject
        gradeSubjectAverageProfileService.updateGradeAverageScoreProfile(itemRequest, gpaProfile);
      }
    }

    // 2. Recalculate GPA Stats
    int passedCredit = 0;
    int attemptingCredit = 0;
    double totalWeightedScore = 0;

    for (GradeSubjectAverageProfile gradeSubject : gpaProfile.getGradeSubjectAverageProfiles()) {
      Double averageSubjectScore = Objects.nonNull(gradeSubject.getImprovementScore())
          ? gradeSubject.getImprovementScore()
          : gradeSubject.getCurrentScore();

      if (Objects.nonNull(averageSubjectScore)) {
        int credit = gradeSubject.getSubjectReference().getSubject().getCredit();

        // Attempted Credit: Counts if there is a score
        attemptingCredit += credit;

        // Weighted Score: Credit * Score
        totalWeightedScore += credit * averageSubjectScore;

        // Passed Credit: If score >= 1.0 (D)
        if (averageSubjectScore >= 1.0) {
          passedCredit += credit;
        }
      }
    }

    // 3. Set Profile Stats
    if (attemptingCredit > 0) {
      double gpa = totalWeightedScore / attemptingCredit;
      gpaProfile.setNumberGpaScore(gpa);
      gpaProfile.setLetterGpaScore(Grade.fromScore(gpa));
    } else {
      gpaProfile.setNumberGpaScore(null);
      gpaProfile.setLetterGpaScore(null);
    }

    gpaProfile.setPassedCredits(passedCredit);
    gpaProfile.setTotalWeightedScore(totalWeightedScore);

    return gpaProfile;
  }
}
