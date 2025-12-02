package com.graduation.project.library.service;

import com.graduation.project.announcement.entity.Faculty;
import com.graduation.project.security.exception.AppException;
import com.graduation.project.security.exception.ErrorCode;
import com.graduation.project.cpa.constant.CohortCode;
import com.graduation.project.announcement.repository.FacultyRepository;
import com.graduation.project.library.entity.Semester;
import com.graduation.project.library.repository.SemesterRepository;
import com.graduation.project.library.entity.Subject;
import com.graduation.project.library.entity.SubjectReference;
import com.graduation.project.library.repository.SubjectReferenceRepository;
import com.graduation.project.library.repository.SubjectRepository;
import com.graduation.project.library.dto.SubjectReferenceRequest;
import com.graduation.project.library.dto.SubjectReferenceResponse;
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
