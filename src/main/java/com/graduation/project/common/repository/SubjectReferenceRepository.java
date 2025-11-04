package com.graduation.project.common.repository;

import com.graduation.project.common.entity.CohortCode;
import com.graduation.project.common.entity.Faculty;
import com.graduation.project.common.entity.Semester;
import com.graduation.project.common.entity.SubjectReference;
import java.util.List;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

public interface SubjectReferenceRepository extends JpaRepository<SubjectReference, UUID> {
  List<SubjectReference> findAllBySemesterAndFacultyAndCohortCode(
      Semester semester, Faculty faculty, CohortCode cohortCode);
}
