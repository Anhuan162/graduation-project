package com.graduation.project.user.service;

import com.graduation.project.auth.exception.AppException;
import com.graduation.project.auth.exception.ErrorCode;
import com.graduation.project.auth.service.CurrentUserService;
import com.graduation.project.common.entity.CpaProfile;
import com.graduation.project.common.entity.GpaProfile;
import com.graduation.project.common.entity.Grade;
import com.graduation.project.common.entity.User;
import com.graduation.project.common.repository.CpaProfileRepository;
import com.graduation.project.user.dto.CpaProfileRequest;
import com.graduation.project.user.dto.CpaProfileResponse;
import com.graduation.project.user.dto.GpaProfileRequest;
import com.graduation.project.user.mapper.CpaProfileMapper;
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
public class CpaProfileService {
  private final CpaProfileRepository cpaProfileRepository;
  private final CurrentUserService currentUserService;
  private final CpaProfileMapper cpaProfileMapper;
  private final GpaProfileService gpaProfileService;

  public CpaProfileResponse initializeCpaProfile(String cpaProfileName) {
    boolean isExistedCpaProfileName = cpaProfileRepository.existsByCpaProfileName(cpaProfileName);
    if (isExistedCpaProfileName) {
      throw new AppException(ErrorCode.CPA_PROFILE_NAME_EXISTED);
    }
    User user = currentUserService.getCurrentUserEntity();

    String studentCode = user.getStudentCode();
    String cpaProfileCode = "CPA" + studentCode + cpaProfileName;

    GpaProfile gpaProfile = gpaProfileService.addGpaProfile(cpaProfileCode, 1);

    CpaProfile cpaProfile =
        CpaProfile.builder().cpaProfileCode(cpaProfileCode).cpaProfileName(studentCode).build();
    cpaProfile.getGpaProfiles().add(gpaProfile);
    cpaProfileRepository.save(cpaProfile);

    return cpaProfileMapper.toCpaProfileResponse(cpaProfile);
  }

  public CpaProfileResponse addGpaProfileForCpaProfile(String cpaProfileId) {
    CpaProfile cpaProfile =
        cpaProfileRepository
            .findById(UUID.fromString(cpaProfileId))
            .orElseThrow(() -> new AppException(ErrorCode.CPA_PROFILE_NOT_FOUND));
    GpaProfile gpaProfile =
        gpaProfileService.addGpaProfile(cpaProfileId, cpaProfile.getGpaProfiles().size() + 1);
    cpaProfile.getGpaProfiles().add(gpaProfile);
    cpaProfileRepository.save(cpaProfile);

    return cpaProfileMapper.toCpaProfileResponse(cpaProfile);
  }

  public CpaProfileResponse deleteGpaProfileInCpaProfile(String cpaProfileId, String gpaProfileId) {
    return null;
  }

  public CpaProfileResponse calculateAverageScore(
      String cpaProfileId, CpaProfileRequest cpaProfileRequest) {
    int accumulatedCredits = 0;
    double totalAccumulatedScore = 0;
    CpaProfile cpaProfile =
        cpaProfileRepository
            .findById(UUID.fromString(cpaProfileId))
            .orElseThrow(() -> new AppException(ErrorCode.CPA_PROFILE_NOT_FOUND));

    List<GpaProfile> gpaProfiles = new ArrayList<>();
    for (GpaProfileRequest gpaProfileRequest : cpaProfileRequest.getGpaProfileRequests()) {
      GpaProfile gpaProfile = gpaProfileService.calculateGpaScores(gpaProfileRequest);
      accumulatedCredits += gpaProfile.getPassedCredits();
      totalAccumulatedScore += gpaProfile.getTotalGpaScoreMultiCredit();
      gpaProfiles.add(gpaProfile);
    }

    cpaProfile.setAccumulatedCredits(accumulatedCredits);
    cpaProfile.setNumberCpaScore(totalAccumulatedScore / accumulatedCredits);
    cpaProfile.setLetterCpaScore(Grade.fromScore(totalAccumulatedScore / accumulatedCredits));

    cpaProfile.setGpaProfiles(gpaProfiles);
    cpaProfileRepository.save(cpaProfile);
    return cpaProfileMapper.toCpaProfileResponse(cpaProfile);
  }
}
