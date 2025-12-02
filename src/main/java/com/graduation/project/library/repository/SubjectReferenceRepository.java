package com.graduation.project.library.repository;

import com.graduation.project.cpa.constant.CohortCode;
import com.graduation.project.announcement.entity.Faculty;
import com.graduation.project.library.entity.Semester;
import com.graduation.project.library.entity.SubjectReference;
import java.util.List;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface SubjectReferenceRepository extends JpaRepository<SubjectReference, UUID> {
  List<SubjectReference> findAllBySemesterAndFacultyAndCohortCode(
      Semester semester, Faculty faculty, CohortCode cohortCode);
}
