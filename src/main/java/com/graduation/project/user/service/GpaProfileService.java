package com.graduation.project.user.service;

import com.graduation.project.auth.exception.AppException;
import com.graduation.project.auth.exception.ErrorCode;
import com.graduation.project.common.entity.*;
import com.graduation.project.common.repository.GpaProfileRepository;
import com.graduation.project.user.dto.GpaProfileRequest;
import com.graduation.project.user.dto.GradeSubjectAverageProfileRequest;
import com.graduation.project.user.mapper.GpaProfileMapper;
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
  private final GpaProfileMapper gpaProfileMapper;

  public GpaProfile addGpaProfile(String cpaProfileCode, int semesterId, CpaProfile cpaProfile) {
    String gpaProfileCode = cpaProfileCode + semesterId;
    String cohortCode = "D" + cpaProfileCode.substring(4, 6);
    String facultyCode = cpaProfileCode.substring(8, 10);
    GpaProfile gpaProfile = GpaProfile.builder().gpaProfileCode(gpaProfileCode).build();

    List<GradeSubjectAverageProfile> gradeSubjectAverageProfiles =
        gradeSubjectAverageProfileService.addGradeSubjectAverageProfileList(
            semesterId, facultyCode, cohortCode, gpaProfile);
    gpaProfile.setGradeSubjectAverageProfiles(gradeSubjectAverageProfiles);
    gpaProfile.setCpaProfile(cpaProfile);
    return gpaProfile;
  }

  public GpaProfile calculateGpaScore(GpaProfileRequest gpaProfileRequest) {
    int passedCredit = 0;
    double totalWeightededScore = 0;

    GpaProfile gpaProfile =
        gpaProfileRepository
            .findById(UUID.fromString(gpaProfileRequest.getId()))
            .orElseThrow(() -> new AppException(ErrorCode.GPA_PROFILE_NOT_FOUND));

    List<GradeSubjectAverageProfile> gradeSubjectAverageProfiles = new ArrayList<>();
    for (GradeSubjectAverageProfileRequest gradeSubjectAverageProfileRequest :
        gpaProfileRequest.getGradeSubjectAverageProfileRequests()) {
      GradeSubjectAverageProfile gradeSubjectAverageProfile =
          gradeSubjectAverageProfileService.updateGradeAverageScoreProfile(
              gradeSubjectAverageProfileRequest);
      gradeSubjectAverageProfile.setGpaProfile(gpaProfile);
      gradeSubjectAverageProfiles.add(gradeSubjectAverageProfile);
    }

    for (GradeSubjectAverageProfile gradeSubjectAverageProfile : gradeSubjectAverageProfiles) {
      Double averageSubjectScore =
          Objects.nonNull(gradeSubjectAverageProfile.getImprovementScore())
              ? gradeSubjectAverageProfile.getImprovementScore()
              : gradeSubjectAverageProfile.getCurrentScore();
      if (Objects.nonNull(averageSubjectScore)) {
        passedCredit += gradeSubjectAverageProfile.getSubjectReference().getSubject().getCredit();
        totalWeightededScore +=
            gradeSubjectAverageProfile.getSubjectReference().getSubject().getCredit()
                * averageSubjectScore;
      }
    }

    gpaProfile.getGradeSubjectAverageProfiles().addAll(gradeSubjectAverageProfiles);
    if (passedCredit == 0) {
      gpaProfile.setNumberGpaScore(null);
      gpaProfile.setLetterGpaScore(null);
    } else {
      gpaProfile.setNumberGpaScore(totalWeightededScore / passedCredit);
      gpaProfile.setLetterGpaScore(Grade.fromScore(totalWeightededScore / passedCredit));
    }

    gpaProfile.setPreviousNumberGpaScore(gpaProfileRequest.getPreviousNumberGpaScore());
    gpaProfile.setPassedCredits(passedCredit);
    gpaProfile.setTotalWeightedScore(totalWeightededScore);

    return gpaProfile;
  }
}
