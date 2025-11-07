package com.graduation.project.admin.mapper;

import com.graduation.project.common.entity.Semester;
import com.graduation.project.admin.dto.SemesterRequest;
import com.graduation.project.admin.dto.SemesterResponse;
import org.mapstruct.Mapper;

@Mapper(componentModel = "spring")
public interface SemesterMapper {

  Semester toSemester(SemesterRequest semesterRequest);

  SemesterResponse toSemesterResponse(Semester semester);
}
