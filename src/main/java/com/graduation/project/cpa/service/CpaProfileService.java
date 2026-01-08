package com.graduation.project.cpa.service;

import com.graduation.project.auth.service.CurrentUserService;
import com.graduation.project.common.entity.User;
import com.graduation.project.cpa.constant.Grade;
import com.graduation.project.cpa.dto.CpaProfileRequest;
import com.graduation.project.cpa.dto.CpaProfileResponse;
import com.graduation.project.cpa.dto.GpaProfileRequest;
import com.graduation.project.cpa.entity.CpaProfile;
import com.graduation.project.cpa.entity.GpaProfile;
import com.graduation.project.cpa.entity.GradeSubjectAverageProfile;
import com.graduation.project.cpa.mapper.CpaProfileMapper;
import com.graduation.project.cpa.repository.CpaProfileRepository;
import com.graduation.project.cpa.repository.GpaProfileRepository;
import com.graduation.project.security.exception.AppException;
import com.graduation.project.security.exception.ErrorCode;
import jakarta.transaction.Transactional;
import java.util.*; // Use wildcard for List, Map, HashMap, UUID, Objects
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
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

    // Check if student code exists first
    String studentCode = user.getStudentCode();
    if (studentCode == null) {
      throw new AppException(ErrorCode.STUDENT_CODE_NULL);
    }

    String cpaProfileCode = "CPA" + studentCode;
    log.info("Initializing CPA profile for user: {}, studentCode: {}, cpaProfileCode: {}",
        user.getId(), studentCode, cpaProfileCode);

    // First check: Look up by user_id
    List<CpaProfile> existingProfilesByUser = cpaProfileRepository.findAllByUserId(user.getId());
    if (!existingProfilesByUser.isEmpty()) {
      log.info("Found existing CPA profile by user_id: {}", existingProfilesByUser.get(0).getId());
      return cpaProfileMapper.toCpaProfileResponse(existingProfilesByUser.get(0));
    }

    // Second check: Look up by cpa_profile_code (unique constraint)
    // This handles edge cases where a profile exists but might not be linked to
    // current user_id
    var existingProfileByCode = cpaProfileRepository.findByCpaProfileCode(cpaProfileCode);
    if (existingProfileByCode.isPresent()) {
      log.warn("Found existing CPA profile by code: {}, but not linked to user_id: {}. Returning existing profile.",
          cpaProfileCode, user.getId());
      return cpaProfileMapper.toCpaProfileResponse(existingProfileByCode.get());
    }

    // No existing profile found, create new one
    log.info("Creating new CPA profile for user: {}, code: {}", user.getId(), cpaProfileCode);
    CpaProfile cpaProfile = CpaProfile.builder()
        .cpaProfileCode(cpaProfileCode)
        .cpaProfileName(studentCode)
        .user(user)
        .build();
    GpaProfile gpaProfile = gpaProfileService.addGpaProfile(studentCode, 1, cpaProfile);

    cpaProfile.getGpaProfiles().add(gpaProfile);
    cpaProfileRepository.save(cpaProfile);

    log.info("Successfully created CPA profile: {}", cpaProfile.getId());
    return cpaProfileMapper.toCpaProfileResponse(cpaProfile);
  }

  public CpaProfileResponse addGpaProfileForCpaProfile(String cpaProfileId) {
    CpaProfile cpaProfile = cpaProfileRepository
        .findById(UUID.fromString(cpaProfileId))
        .orElseThrow(() -> new AppException(ErrorCode.CPA_PROFILE_NOT_FOUND));
    GpaProfile gpaProfile = gpaProfileService.addGpaProfile(
        cpaProfile.getUser().getStudentCode(),
        cpaProfile.getGpaProfiles().size() + 1,
        cpaProfile);
    cpaProfile.getGpaProfiles().add(gpaProfile);
    cpaProfileRepository.save(cpaProfile);

    return cpaProfileMapper.toCpaProfileResponse(cpaProfile);
  }

  public CpaProfileResponse deleteGpaProfileInCpaProfile(String cpaProfileId, String gpaProfileId) {
    CpaProfile cpaProfile = cpaProfileRepository.findById(UUID.fromString(cpaProfileId)).orElseThrow();
    GpaProfile gpaProfile = gpaProfileRepository.findById(UUID.fromString(gpaProfileId)).orElseThrow();
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

  public CpaProfileResponse updateCpaProfile(
      String cpaProfileId, CpaProfileRequest cpaProfileRequest) {
    CpaProfile cpaProfile = cpaProfileRepository
        .findById(UUID.fromString(cpaProfileId))
        .orElseThrow(() -> new AppException(ErrorCode.CPA_PROFILE_NOT_FOUND));

    // 1. Process Updates
    for (GpaProfileRequest gpaProfileRequest : cpaProfileRequest.getGpaProfileRequests()) {
      gpaProfileService.updateGpaProfile(gpaProfileRequest);
    }

    // 2. Recalculate Totals based on BEST GRADE per SUBJECT across ALL semesters
    List<GpaProfile> allGpaProfiles = cpaProfile.getGpaProfiles();

    // Map: SubjectId -> GradeSubjectAverageProfile (Best Performance)
    Map<UUID, GradeSubjectAverageProfile> bestSubjectGrades = new HashMap<>();

    for (GpaProfile gp : allGpaProfiles) {
      for (GradeSubjectAverageProfile entry : gp.getGradeSubjectAverageProfiles()) {
        if (entry.getSubjectReference() == null
            || entry.getSubjectReference().getSubject() == null) {
          continue; // Skip invalid records
        }

        UUID subjectId = entry.getSubjectReference().getSubject().getId();
        Double effectiveScore = getEffectiveScore(entry);

        if (!bestSubjectGrades.containsKey(subjectId)) {
          bestSubjectGrades.put(subjectId, entry);
        } else {
          // Compare with existing best
          GradeSubjectAverageProfile existingBest = bestSubjectGrades.get(subjectId);
          Double existingScore = getEffectiveScore(existingBest);

          if (effectiveScore > existingScore) {
            bestSubjectGrades.put(subjectId, entry);
          }
        }
      }
    }

    // 3. Aggregate Stats from Unique Best Subjects
    int totalPassedCredits = 0;
    int totalAttemptedCredits = 0;
    double totalAccumulatedScore = 0;

    for (GradeSubjectAverageProfile bestEntry : bestSubjectGrades.values()) {
      int credit = bestEntry.getSubjectReference().getSubject().getCredit();
      Double score = getEffectiveScore(bestEntry);

      totalAttemptedCredits += credit;
      totalAccumulatedScore += (score * credit);

      if (score >= 1.0) { // D or higher
        totalPassedCredits += credit;
      }
    }

    cpaProfile.setAccumulatedCredits(totalPassedCredits);
    cpaProfile.setTotalAccumulatedScore(totalAccumulatedScore);

    if (totalAttemptedCredits == 0) {
      cpaProfile.setNumberCpaScore(0.0);
      cpaProfile.setLetterCpaScore(null);
    } else {
      double cpa = totalAccumulatedScore / totalAttemptedCredits;
      cpaProfile.setNumberCpaScore(cpa);
      cpaProfile.setLetterCpaScore(Grade.fromScore(cpa));
    }

    cpaProfileRepository.save(cpaProfile);
    return cpaProfileMapper.toCpaProfileResponse(cpaProfile);
  }

  private Double getEffectiveScore(GradeSubjectAverageProfile entry) {
    if (Objects.nonNull(entry.getImprovementScore())) {
      return entry.getImprovementScore();
    }
    return entry.getCurrentScore() != null ? entry.getCurrentScore() : 0.0;
  }

  public void deleteCpaProfile(String cpaProfileId) {
    if (cpaProfileRepository.findById(UUID.fromString(cpaProfileId)).isEmpty()) {
      throw new AppException(ErrorCode.CPA_PROFILE_NOT_FOUND);
    }
    cpaProfileRepository.deleteById(UUID.fromString(cpaProfileId));
  }

  public Page<CpaProfileResponse> getCpaProfiles(Pageable pageable) {
    Page<CpaProfile> cpaProfiles = cpaProfileRepository.findAll(pageable);
    return cpaProfiles.map(cpaProfileMapper::toCpaProfileInfoResponse);
  }

  public CpaProfileResponse getCpaProfile(String cpaProfileId) {
    CpaProfile cpaProfile = cpaProfileRepository
        .findById(UUID.fromString(cpaProfileId))
        .orElseThrow(() -> new AppException(ErrorCode.CPA_PROFILE_NOT_FOUND));
    return cpaProfileMapper.toCpaProfileResponse(cpaProfile);
  }
}
