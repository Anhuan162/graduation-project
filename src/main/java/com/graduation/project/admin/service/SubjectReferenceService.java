package com.graduation.project.admin.service;

import com.graduation.project.auth.exception.AppException;
import com.graduation.project.auth.exception.ErrorCode;
import com.graduation.project.common.entity.*;
import com.graduation.project.common.repository.FacultyRepository;
import com.graduation.project.common.repository.SemesterRepository;
import com.graduation.project.common.repository.SubjectReferenceRepository;
import com.graduation.project.common.repository.SubjectRepository;
import com.graduation.project.admin.dto.SubjectReferenceRequest;
import com.graduation.project.admin.dto.SubjectReferenceResponse;
import jakarta.transaction.Transactional;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Transactional
@Service
@RequiredArgsConstructor
@Slf4j
public class SubjectReferenceService {

  private final SubjectReferenceRepository subjectReferenceRepository;
  private final SubjectRepository subjectRepository;
  private final FacultyRepository facultyRepository;
  private final SemesterRepository semesterRepository;

  public SubjectReferenceResponse createSubjectReference(
      SubjectReferenceRequest subjectReferenceRequest) {
    Subject subject =
        subjectRepository
            .findById(UUID.fromString(subjectReferenceRequest.getSubjectId()))
            .orElseThrow(() -> new AppException(ErrorCode.SUBJECT_NOT_FOUND));

    Faculty faculty =
        facultyRepository
            .findById(UUID.fromString(subjectReferenceRequest.getFacultyId()))
            .orElseThrow(() -> new AppException(ErrorCode.FACULTY_NOT_FOUND));

    Semester semester =
        semesterRepository
            .findById(subjectReferenceRequest.getSemesterId())
            .orElseThrow(() -> new AppException(ErrorCode.SEMESTER_NOT_FOUND));

    SubjectReference subjectReference = new SubjectReference();
    subjectReference.setSubject(subject);
    subjectReference.setFaculty(faculty);
    subjectReference.setSemester(semester);
    subjectReference.setCohortCode(CohortCode.valueOf(subjectReferenceRequest.getCohortCode()));
    subjectReferenceRepository.save(subjectReference);
    return SubjectReferenceResponse.toSubjectReferenceResponse(subjectReference);
  }

  public SubjectReferenceResponse getSubjectReference(String subjectReferenceId) {
    SubjectReference subjectReference =
        subjectReferenceRepository
            .findById(UUID.fromString(subjectReferenceId))
            .orElseThrow(() -> new AppException(ErrorCode.SUBJECT_REFERENCE_NOT_FOUND));
    return SubjectReferenceResponse.toSubjectReferenceResponse(subjectReference);
  }
}
