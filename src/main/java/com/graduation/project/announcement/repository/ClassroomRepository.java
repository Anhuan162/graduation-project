package com.graduation.project.announcement.repository;

import com.graduation.project.announcement.entity.Classroom;
import com.graduation.project.cpa.constant.CohortCode;
import java.util.List;
import java.util.UUID;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.Collection;
import java.util.Set;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

@Repository
public interface ClassroomRepository extends JpaRepository<Classroom, UUID> {
  List<Classroom> findBySchoolYearCodeIn(List<CohortCode> schoolYearCode);

  List<Classroom> findByFacultyIdIn(List<UUID> facultyIds);

  boolean existsByClassCode(String classCode);

  Page<Classroom> findAll(Specification<Classroom> specs, Pageable pageable);

  @Query("SELECT c FROM Classroom c "
      + "INNER JOIN Faculty f ON c.faculty.id = f.id "
      + "WHERE (:facultyId IS NULL OR f.id = :facultyId) AND"
      + "(:classCode IS NULL OR c.classCode = :classCode) AND"
      + "(:schoolYearCode IS NULL OR c.schoolYearCode = :schoolYearCode)")
  Page<Classroom> searchClassrooms(
      @Param("classCode") String classCode,
      @Param("facultyId") UUID facultyId,
      @Param("schoolYearCode") CohortCode schoolYearCode,
      Pageable pageable);

  List<Classroom> findAllByFaculty_IdAndSchoolYearCode(UUID facultyId, CohortCode schoolYearCode);

  @Query("SELECT c.classCode FROM Classroom c WHERE c.schoolYearCode IN :codes")
  Set<String> findClassCodesBySchoolYearCodeIn(@Param("codes") Collection<CohortCode> codes);

  @Query("SELECT c.classCode FROM Classroom c WHERE c.faculty.id IN :ids")
  Set<String> findClassCodesByFacultyIdIn(@Param("ids") Collection<UUID> ids);

  @Query("SELECT c.classCode FROM Classroom c")
  Set<String> findAllClassCodes();
}
