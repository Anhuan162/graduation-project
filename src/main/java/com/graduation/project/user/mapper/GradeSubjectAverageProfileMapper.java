package com.graduation.project.user.mapper;

import com.graduation.project.common.entity.GradeSubjectAverageProfile;
import com.graduation.project.user.dto.GradeSubjectAverageProfileRequest;
import com.graduation.project.user.dto.GradeSubjectAverageProfileResponse;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface GradeSubjectAverageProfileMapper {


  GradeSubjectAverageProfile toGradeSubjectAverageProfile(
      GradeSubjectAverageProfileRequest gradeSubjectAverageProfileRequest);

  @Mapping(target = "subjectName", source = "subjectReference.subject.subjectName")
  @Mapping(target = "subjectCode", source = "subjectReference.subject.subjectCode")
  @Mapping(target = "subjectId", source = "subjectReference.subject.id")
  @Mapping(target = "credit", source = "subjectReference.subject.credit")
  GradeSubjectAverageProfileResponse toGradeSubjectAverageProfileResponse(
      GradeSubjectAverageProfile gradeSubjectAverageProfile);
}
