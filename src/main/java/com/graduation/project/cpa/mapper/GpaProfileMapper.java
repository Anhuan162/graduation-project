package com.graduation.project.cpa.mapper;

import com.graduation.project.cpa.entity.GpaProfile;
import com.graduation.project.cpa.dto.GpaProfileRequest;
import com.graduation.project.cpa.dto.GpaProfileResponse;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring", uses = GradeSubjectAverageProfileMapper.class)
public interface GpaProfileMapper {
  @Mapping(target = "gradeSubjectAverageProfiles", source = "gradeSubjectAverageProfileRequests")
  GpaProfile toGpaProfile(GpaProfileRequest gpaProfileRequest);

  @Mapping(target = "gradeSubjectAverageProfileResponses", source = "gradeSubjectAverageProfiles")
  GpaProfileResponse toGpaProfileResponse(GpaProfile gpaProfile);
}
