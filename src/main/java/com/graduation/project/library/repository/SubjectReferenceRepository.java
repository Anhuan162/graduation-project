package com.graduation.project.library.repository;

import com.graduation.project.announcement.entity.Faculty;
import com.graduation.project.cpa.constant.CohortCode;
import com.graduation.project.library.entity.Semester;
import com.graduation.project.library.entity.SubjectReference;
import jakarta.validation.constraints.NotNull;
import java.util.List;
import java.util.UUID;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface SubjectReferenceRepository extends JpaRepository<SubjectReference, UUID> {
  List<SubjectReference> findAllBySemesterAndFacultyAndCohortCode(
      Semester semester, Faculty faculty, CohortCode cohortCode);

  Page<SubjectReference> findAll(Specification<SubjectReference> specification, Pageable pageable);

  boolean existsBySubjectIdAndFacultyIdAndSemesterIdAndCohortCode(
      UUID subject_id, UUID faculty_id, Integer semester_id, CohortCode cohortCode);

  java.util.Optional<SubjectReference> findBySubject_IdAndSemester_IdAndFaculty_FacultyCodeAndCohortCode(
      UUID subjectId,
      Integer semesterId,
      String facultyCode,
      CohortCode cohortCode);
}
