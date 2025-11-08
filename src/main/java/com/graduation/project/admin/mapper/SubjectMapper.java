package com.graduation.project.admin.mapper;

import com.graduation.project.common.entity.Subject;
import com.graduation.project.admin.dto.SubjectRequest;
import com.graduation.project.admin.dto.SubjectResponse;
import java.time.LocalDateTime;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(
    componentModel = "spring",
    imports = {LocalDateTime.class})
public interface SubjectMapper {

  @Mapping(target = "id", ignore = true)
  @Mapping(target = "subjectReferences", ignore = true)
  @Mapping(target = "documents", ignore = true)
  @Mapping(target = "createdDate", expression = "java(LocalDateTime.now())")
  @Mapping(target = "lastModifiedDate", expression = "java(LocalDateTime.now())")
  Subject toSubject(SubjectRequest request);

  SubjectResponse toSubjectResponse(Subject subject);
}
