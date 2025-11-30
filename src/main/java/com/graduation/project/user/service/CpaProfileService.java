package com.graduation.project.user.service;

import com.graduation.project.auth.exception.AppException;
import com.graduation.project.auth.exception.ErrorCode;
import com.graduation.project.auth.service.CurrentUserService;
import com.graduation.project.common.entity.CpaProfile;
import com.graduation.project.common.entity.GpaProfile;
import com.graduation.project.common.entity.Grade;
import com.graduation.project.common.entity.User;
import com.graduation.project.common.repository.CpaProfileRepository;
import com.graduation.project.common.repository.GpaProfileRepository;
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
  private final GpaProfileRepository gpaProfileRepository;

  public CpaProfileResponse initializeCpaProfile() {
    User user = currentUserService.getCurrentUserEntity();

    String studentCode = user.getStudentCode();
    if (studentCode == null) {
      throw new AppException(ErrorCode.STUDENT_CODE_NULL);
    }
    String cpaProfileCode = "CPA" + studentCode;
    CpaProfile cpaProfile =
        CpaProfile.builder()
            .cpaProfileCode(cpaProfileCode)
            .cpaProfileName(studentCode)
            .user(user)
            .build();
    GpaProfile gpaProfile = gpaProfileService.addGpaProfile(studentCode, 1, cpaProfile);

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
        gpaProfileService.addGpaProfile(
            cpaProfile.getUser().getStudentCode(),
            cpaProfile.getGpaProfiles().size() + 1,
            cpaProfile);
    cpaProfile.getGpaProfiles().add(gpaProfile);
    cpaProfileRepository.save(cpaProfile);

    return cpaProfileMapper.toCpaProfileResponse(cpaProfile);
  }

  public CpaProfileResponse deleteGpaProfileInCpaProfile(String cpaProfileId, String gpaProfileId) {
    CpaProfile cpaProfile =
        cpaProfileRepository.findById(UUID.fromString(cpaProfileId)).orElseThrow();
    GpaProfile gpaProfile =
        gpaProfileRepository.findById(UUID.fromString(gpaProfileId)).orElseThrow();
    int passedCredits = gpaProfile.getPassedCredits();
    Double totalWeightedScore = gpaProfile.getTotalWeightedScore();
    int previousAccumulatedCredits = cpaProfile.getAccumulatedCredits();
    Double previousTotalAccumulatedScore = cpaProfile.getTotalAccumulatedScore();
    gpaProfile.setCpaProfile(null);
    cpaProfile.getGpaProfiles().remove(gpaProfile);

    int accumulatedCredits = previousAccumulatedCredits - passedCredits;
    double totalAccumulatedScore = previousTotalAccumulatedScore - totalWeightedScore;

    cpaProfile.setAccumulatedCredits(accumulatedCredits);
    cpaProfile.setTotalAccumulatedScore(totalAccumulatedScore);
    if (accumulatedCredits == 0) {
      cpaProfile.setNumberCpaScore(null);
      cpaProfile.setLetterCpaScore(null);
    } else {
      cpaProfile.setNumberCpaScore(totalAccumulatedScore / accumulatedCredits);
      cpaProfile.setLetterCpaScore(Grade.fromScore(totalAccumulatedScore / accumulatedCredits));
    }

    cpaProfileRepository.save(cpaProfile);
    return cpaProfileMapper.toCpaProfileResponse(cpaProfile);
  }

  public CpaProfileResponse calculateCpaScore(
      String cpaProfileId, CpaProfileRequest cpaProfileRequest) {
    int accumulatedCredits = 0;
    double totalAccumulatedScore = 0;
    CpaProfile cpaProfile =
        cpaProfileRepository
            .findById(UUID.fromString(cpaProfileId))
            .orElseThrow(() -> new AppException(ErrorCode.CPA_PROFILE_NOT_FOUND));

    List<GpaProfile> gpaProfiles = new ArrayList<>();
    for (GpaProfileRequest gpaProfileRequest : cpaProfileRequest.getGpaProfileRequests()) {
      GpaProfile gpaProfile = gpaProfileService.calculateGpaScore(gpaProfileRequest);
      gpaProfile.setCpaProfile(cpaProfile);
      accumulatedCredits += gpaProfile.getPassedCredits();
      totalAccumulatedScore += gpaProfile.getTotalWeightedScore();
      gpaProfiles.add(gpaProfile);
    }

    cpaProfile.setAccumulatedCredits(accumulatedCredits);
    cpaProfile.setTotalAccumulatedScore(totalAccumulatedScore);
    if (accumulatedCredits == 0) {
      cpaProfile.setNumberCpaScore(null);
      cpaProfile.setLetterCpaScore(null);
    } else {
      cpaProfile.setNumberCpaScore(totalAccumulatedScore / accumulatedCredits);
      cpaProfile.setLetterCpaScore(Grade.fromScore(totalAccumulatedScore / accumulatedCredits));
    }

    cpaProfile.getGpaProfiles().addAll(gpaProfiles);
    cpaProfileRepository.save(cpaProfile);
    return cpaProfileMapper.toCpaProfileResponse(cpaProfile);
  }

  public void deleteCpaProfile(String cpaProfileId) {
    if (cpaProfileRepository.findById(UUID.fromString(cpaProfileId)).isEmpty()) {
      throw new AppException(ErrorCode.CPA_PROFILE_NOT_FOUND);
    }
    cpaProfileRepository.deleteById(UUID.fromString(cpaProfileId));
  }

  public List<CpaProfileResponse> getCpaProfiles() {
    User user = currentUserService.getCurrentUserEntity();
    List<CpaProfile> cpaProfiles = cpaProfileRepository.findAllByUserId(user.getId());
    return cpaProfiles.stream().map(cpaProfileMapper::toCpaProfileInfoResponse).toList();
  }

  public CpaProfileResponse getCpaProfile(String cpaProfileId) {
    CpaProfile cpaProfile =
        cpaProfileRepository
            .findById(UUID.fromString(cpaProfileId))
            .orElseThrow(() -> new AppException(ErrorCode.CPA_PROFILE_NOT_FOUND));
    return cpaProfileMapper.toCpaProfileResponse(cpaProfile);
  }
}
