package com.graduation.project.user.service;

import com.graduation.project.auth.exception.AppException;
import com.graduation.project.auth.exception.ErrorCode;
import com.graduation.project.auth.service.CurrentUserService;
import com.graduation.project.common.entity.CpaProfile;
import com.graduation.project.common.entity.GpaProfile;
import com.graduation.project.common.entity.User;
import com.graduation.project.common.repository.CpaProfileRepository;
import com.graduation.project.user.dto.CpaProfileRequest;
import com.graduation.project.user.dto.CpaProfileResponse;
import com.graduation.project.user.mapper.CpaProfileMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.springframework.stereotype.Service;

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

    GpaProfile gpaProfile = gpaProfileService.addGpaProfile(cpaProfileCode, 0);

    CpaProfileRequest request =
        CpaProfileRequest.builder()
            .cpaProfileCode(cpaProfileCode)
            .cpaProfileName(cpaProfileName)
            .build();

    CpaProfile cpaProfile = cpaProfileMapper.toCpaProfile(request);
    return cpaProfileMapper.toCpaProfileResponse(cpaProfile);
  }
}
