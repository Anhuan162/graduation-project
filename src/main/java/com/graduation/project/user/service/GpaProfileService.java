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

  public GpaProfile addGpaProfile(String cpaProfileCode, int previousSemesterId) {
    String gpaProfileCode = cpaProfileCode + previousSemesterId;
    String cohortCode = "D" + cpaProfileCode.substring(4, 6);
    String facultyCode = cpaProfileCode.substring(8, 10);

    List<GradeSubjectAverageProfile> gradeSubjectAverageProfiles =
        gradeSubjectAverageProfileService.addGradeSubjectAverageProfileList(
            previousSemesterId, facultyCode, cohortCode);

    return GpaProfile.builder()
        .gpaProfileCode(gpaProfileCode)
        .gradeSubjectAverageProfiles(gradeSubjectAverageProfiles)
        .build();
  }

  public GpaProfile calculateGpaScores(GpaProfileRequest gpaProfileRequest) {
    int passedCredit = 0;
    double totalPassedScore = 0;

    List<GradeSubjectAverageProfile> gradeSubjectAverageProfiles = new ArrayList<>();
    for (GradeSubjectAverageProfileRequest gradeSubjectAverageProfileRequest :
        gpaProfileRequest.getGradeSubjectAverageProfileRequests()) {
      GradeSubjectAverageProfile gradeSubjectAverageProfile =
          gradeSubjectAverageProfileService.updateGradeAverageScoreProfile(
              gradeSubjectAverageProfileRequest);
      String averageSubjectScore =
          Objects.nonNull(gradeSubjectAverageProfileRequest.getImprovementScore())
              ? gradeSubjectAverageProfileRequest.getImprovementScore()
              : gradeSubjectAverageProfileRequest.getCurrentScore();
      passedCredit += gradeSubjectAverageProfile.getSubjectReference().getSubject().getCredit();
      totalPassedScore +=
          gradeSubjectAverageProfile.getSubjectReference().getSubject().getCredit()
              * Grade.toScore(averageSubjectScore);
      gradeSubjectAverageProfiles.add(gradeSubjectAverageProfile);
    }

    GpaProfile gpaProfile =
        gpaProfileRepository
            .findById(UUID.fromString(gpaProfileRequest.getGpaProfileId()))
            .orElseThrow(() -> new AppException(ErrorCode.GPA_PROFILE_NOT_FOUND));

    gpaProfile.setGradeSubjectAverageProfiles(gradeSubjectAverageProfiles);
    gpaProfile.setNumberGpaScore(totalPassedScore / passedCredit);
    gpaProfile.setLetterGpaScore(Grade.fromScore(totalPassedScore / passedCredit));
    gpaProfile.setPreviousNumberGpaScore(gpaProfileRequest.getPreviousNumberGpaScore());
    gpaProfile.setTotalGpaScoreMultiCredit(totalPassedScore);

    return gpaProfile;
  }
}
