package com.graduation.project.cpa.mapper;

import com.graduation.project.cpa.entity.CpaProfile;
import com.graduation.project.cpa.dto.CpaProfileRequest;
import com.graduation.project.cpa.dto.CpaProfileResponse;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring", uses = GpaProfileMapper.class)
public interface CpaProfileMapper {

  @Mapping(target = "gpaProfiles", source = "gpaProfileRequests")
  CpaProfile toCpaProfile(CpaProfileRequest cpaProfileRequest);

  @Mapping(target = "gpaProfiles", source = "gpaProfiles")
  CpaProfileResponse toCpaProfileResponse(CpaProfile cpaProfile);

  @Mapping(target = "gpaProfiles", ignore = true)
  CpaProfileResponse toCpaProfileInfoResponse(CpaProfile cpaProfile);
}
