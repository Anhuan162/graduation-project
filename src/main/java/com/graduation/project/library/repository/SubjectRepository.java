package com.graduation.project.library.repository;

import com.graduation.project.cpa.constant.CohortCode;
import com.graduation.project.library.entity.Subject;
import java.util.List;
import java.util.UUID;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

@Repository
public interface SubjectRepository extends JpaRepository<Subject, UUID> {

  //    ThieuNN
  @Query(
      "SELECT distinct s from Subject as s "
          + " inner join SubjectReference as sref on sref.subject = s "
          + " inner join Semester as se on se = sref.semester "
          + " inner join Faculty as fa on sref.faculty = fa "
          + " where (:facultyId is null or fa.id = :facultyId) and "
          + " ( :semesterId is null or se.id = :semesterId)")
  public List<Subject> findSubjectByFacultyIdAndSemesterId(
      @Param("facultyId") UUID facultyId, @Param("semesterId") UUID semesterId);

  @Query(
      "SELECT distinct s from Subject as s "
              + " inner join SubjectReference as sref on sref.subject = s "
              + " inner join Semester as se on se = sref.semester "
              + " inner join Faculty as fa on sref.faculty = fa "
              + " where (:facultyId is null or fa.id = :facultyId) and "
              + " ( :semesterId is null or se.id = :semesterId) and"
              + " ( :cohortCode is null or sref.cohortCode = :cohortCode) and"
              + " ( :subjectName is null or s.subjectName like concat('%' ,:subjectName, '%'))")
  Page<Subject> searchSubjects(
      @Param("facultyId") UUID facultyId,
      @Param("semesterId") Integer semesterId,
      @Param("cohortCode") CohortCode cohortCode,
      @Param("subjectName") String subjectName,
      Pageable pageable);
}
