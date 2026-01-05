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

  public GpaProfile addGpaProfile(String studentCode, int semesterId, CpaProfile cpaProfile) {
    String gpaProfileCode = "GPA" + studentCode + semesterId;
    String cohortCode = "D" + studentCode.substring(1, 3);
    String facultyCode = studentCode.substring(5, 7);
    GpaProfile gpaProfile = GpaProfile.builder().gpaProfileCode(gpaProfileCode).build();

    List<GradeSubjectAverageProfile> gradeSubjectAverageProfiles = gradeSubjectAverageProfileService
        .addGradeSubjectAverageProfileList(
            semesterId, facultyCode, cohortCode, gpaProfile);
    gpaProfile.setGradeSubjectAverageProfiles(gradeSubjectAverageProfiles);
    gpaProfile.setCpaProfile(cpaProfile);
    return gpaProfile;
  }

  public GpaProfile updateGpaProfile(GpaProfileRequest gpaProfileRequest) {
    int passedCredit = 0;
    double totalWeightedScore = 0;

    GpaProfile gpaProfile = gpaProfileRepository
        .findById(UUID.fromString(gpaProfileRequest.getId()))
        .orElseThrow(() -> new AppException(ErrorCode.GPA_PROFILE_NOT_FOUND));

    List<GradeSubjectAverageProfile> gradeSubjectAverageProfiles = new ArrayList<>();
    for (GradeSubjectAverageProfileRequest gradeSubjectAverageProfileRequest : gpaProfileRequest
        .getGradeSubjectAverageProfileRequests()) {
      GradeSubjectAverageProfile gradeSubjectAverageProfile = gradeSubjectAverageProfileService
          .updateGradeAverageScoreProfile(
              gradeSubjectAverageProfileRequest);
      gradeSubjectAverageProfile.setGpaProfile(gpaProfile);
      gradeSubjectAverageProfiles.add(gradeSubjectAverageProfile);
    }

    for (GradeSubjectAverageProfile gradeSubjectAverageProfile : gradeSubjectAverageProfiles) {
      Double averageSubjectScore = Objects.nonNull(gradeSubjectAverageProfile.getImprovementScore())
          ? gradeSubjectAverageProfile.getImprovementScore()
          : gradeSubjectAverageProfile.getCurrentScore();
      if (Objects.nonNull(averageSubjectScore)) {
        passedCredit += gradeSubjectAverageProfile.getSubjectReference().getSubject().getCredit();
        totalWeightedScore += gradeSubjectAverageProfile.getSubjectReference().getSubject().getCredit()
            * averageSubjectScore;
      }
    }

    gpaProfile.getGradeSubjectAverageProfiles().addAll(gradeSubjectAverageProfiles);
    if (passedCredit == 0) {
      gpaProfile.setNumberGpaScore(null);
      gpaProfile.setLetterGpaScore(null);
    } else {
      gpaProfile.setNumberGpaScore(totalWeightedScore / passedCredit);
      gpaProfile.setLetterGpaScore(Grade.fromScore(totalWeightedScore / passedCredit));
    }

    gpaProfile.setPreviousNumberGpaScore(gpaProfileRequest.getPreviousNumberGpaScore());
    gpaProfile.setPassedCredits(passedCredit);
    gpaProfile.setTotalWeightedScore(totalWeightedScore);

    return gpaProfile;
  }
}
