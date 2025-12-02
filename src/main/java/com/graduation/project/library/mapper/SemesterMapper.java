package com.graduation.project.library.mapper;

import com.graduation.project.library.entity.Semester;
import com.graduation.project.library.dto.SemesterRequest;
import com.graduation.project.library.dto.SemesterResponse;
import org.mapstruct.Mapper;

@Mapper(componentModel = "spring")
public interface SemesterMapper {

  Semester toSemester(SemesterRequest semesterRequest);

  SemesterResponse toSemesterResponse(Semester semester);
}
