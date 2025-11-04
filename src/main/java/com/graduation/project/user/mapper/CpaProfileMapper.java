package com.graduation.project.user.mapper;

import com.graduation.project.common.entity.CpaProfile;
import com.graduation.project.user.dto.CpaProfileRequest;
import com.graduation.project.user.dto.CpaProfileResponse;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface CpaProfileMapper {

  @Mapping(target = "gpaProfiles", source = "gpaProfileRequests")
  CpaProfile toCpaProfile(CpaProfileRequest cpaProfileRequest);

  @Mapping(target = "gpaProfileResponses", source = "gpaProfiles")
  CpaProfileResponse toCpaProfileResponse(CpaProfile cpaProfile);
}
