package com.graduation.project.library.service;

import com.graduation.project.announcement.entity.Faculty;
import com.graduation.project.announcement.repository.FacultyRepository;
import com.graduation.project.cpa.constant.CohortCode;
import com.graduation.project.library.dto.SubjectReferenceRequest;
import com.graduation.project.library.dto.SubjectReferenceResponse;
import com.graduation.project.library.entity.Semester;
import com.graduation.project.library.entity.Subject;
import com.graduation.project.library.entity.SubjectReference;
import com.graduation.project.library.repository.SemesterRepository;
import com.graduation.project.library.repository.SubjectReferenceRepository;
import com.graduation.project.library.repository.SubjectRepository;
import com.graduation.project.security.exception.AppException;
import com.graduation.project.security.exception.ErrorCode;
import jakarta.persistence.criteria.Join;
import jakarta.persistence.criteria.JoinType;
import jakarta.persistence.criteria.Predicate;
import jakarta.transaction.Transactional;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.coyote.BadRequestException;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
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

  public SubjectReferenceResponse createSubjectReference(SubjectReferenceRequest req) {

    boolean exists = subjectReferenceRepository
        .existsBySubjectIdAndFacultyIdAndSemesterIdAndCohortCode(
            req.getSubjectId(),
            req.getFacultyId(),
            req.getSemesterId(),
            req.getCohortCode());

    if (exists) {
      throw new AppException(ErrorCode.CONFLICT);
    }

    Subject subject = subjectRepository.findById(req.getSubjectId())
        .orElseThrow(() -> new AppException(ErrorCode.SUBJECT_NOT_FOUND));

    Faculty faculty = facultyRepository.findById(req.getFacultyId())
        .orElseThrow(() -> new AppException(ErrorCode.FACULTY_NOT_FOUND));

    Semester semester = semesterRepository.findById(req.getSemesterId())
        .orElseThrow(() -> new AppException(ErrorCode.SEMESTER_NOT_FOUND));

    SubjectReference subjectReference = new SubjectReference();
    subjectReference.setSubject(subject);
    subjectReference.setFaculty(faculty);
    subjectReference.setSemester(semester);
    subjectReference.setCohortCode(req.getCohortCode());

    subjectReferenceRepository.save(subjectReference);
    return SubjectReferenceResponse.toSubjectReferenceResponse(subjectReference);
  }

  public SubjectReferenceResponse getSubjectReference(UUID subjectReferenceId) {
    SubjectReference subjectReference = subjectReferenceRepository
        .findById(subjectReferenceId)
        .orElseThrow(() -> new AppException(ErrorCode.SUBJECT_REFERENCE_NOT_FOUND));
    return SubjectReferenceResponse.toSubjectReferenceResponse(subjectReference);
  }

  public Page<SubjectReferenceResponse> searchSubjectReferences(
      UUID facultyId,
      Integer semesterId,
      CohortCode cohortCode,
      UUID subjectId,
      Pageable pageable) {

    Specification<SubjectReference> specification = (root, query, cb) -> {

      // JOIN
      Join<Object, Object> facultyJoin = root.join("faculty", JoinType.LEFT);
      Join<Object, Object> subjectJoin = root.join("subject", JoinType.LEFT);
      Join<Object, Object> semesterJoin = root.join("semester", JoinType.LEFT);

      List<Predicate> predicates = new ArrayList<>();

      if (Objects.nonNull(facultyId)) {
        predicates.add(cb.equal(facultyJoin.get("id"), facultyId));
      }

      if (Objects.nonNull(semesterId)) {
        predicates.add(cb.equal(semesterJoin.get("id"), semesterId));
      }

      if (Objects.nonNull(cohortCode)) {
        predicates.add(cb.equal(root.get("cohortCode"), cohortCode));
      }

      if (Objects.nonNull(subjectId)) {
        predicates.add(cb.equal(subjectJoin.get("id"), subjectId));
      }

      return cb.and(predicates.toArray(new Predicate[0]));
    };

    Page<SubjectReference> subjectReferences = subjectReferenceRepository.findAll(specification, pageable);

    return subjectReferences.map(SubjectReferenceResponse::toSubjectReferenceResponse);
  }

  public void deleteSubjectReference(UUID subjectReferenceId) {
    SubjectReference subjectReference = subjectReferenceRepository
        .findById(subjectReferenceId)
        .orElseThrow(() -> new AppException(ErrorCode.SUBJECT_REFERENCE_NOT_FOUND));
    subjectReferenceRepository.delete(subjectReference);
  }
}
