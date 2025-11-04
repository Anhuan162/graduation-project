package com.graduation.project.user.mapper;

import com.graduation.project.common.entity.GradeSubjectAverageProfile;
import com.graduation.project.user.dto.GradeSubjectAverageProfileRequest;
import com.graduation.project.user.dto.GradeSubjectAverageProfileResponse;
import org.mapstruct.Mapper;

@Mapper(componentModel = "spring")
public interface GradeSubjectAverageProfileMapper {

  GradeSubjectAverageProfile toGradeSubjectAverageProfile(
      GradeSubjectAverageProfileRequest gradeSubjectAverageProfileRequest);


  GradeSubjectAverageProfileResponse toGradeSubjectAverageProfileResponse(
      GradeSubjectAverageProfile gradeSubjectAverageProfile);
}
