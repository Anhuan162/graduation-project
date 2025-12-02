package com.graduation.project.library.service;

import com.graduation.project.auth.exception.AppException;
import com.graduation.project.auth.exception.ErrorCode;
import com.graduation.project.library.entity.Semester;
import com.graduation.project.library.repository.SemesterRepository;
import com.graduation.project.library.dto.SemesterRequest;
import com.graduation.project.library.dto.SemesterResponse;
import com.graduation.project.library.mapper.SemesterMapper;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Transactional
@Service
@RequiredArgsConstructor
@Slf4j
public class SemesterService {

  private final SemesterRepository semesterRepository;
  private final SemesterMapper semesterMapper;

  public SemesterResponse createSemester(SemesterRequest semesterRequest) {
    Semester semester = semesterMapper.toSemester(semesterRequest);
    semesterRepository.save(semester);

    return semesterMapper.toSemesterResponse(semester);
  }

  public SemesterResponse getSemester(String semesterId) {
    Semester semester =
        semesterRepository
            .findById(Integer.valueOf(semesterId))
            .orElseThrow(() -> new AppException(ErrorCode.SEMESTER_NOT_FOUND));
    return semesterMapper.toSemesterResponse(semester);
  }
}
