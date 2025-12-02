package com.graduation.project.announcement.repository;

import com.graduation.project.announcement.entity.Classroom;
import java.util.List;
import java.util.UUID;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface ClassroomRepository extends JpaRepository<Classroom, UUID> {
  List<Classroom> findBySchoolYearCodeIn(List<String> schoolYearCode);

  List<Classroom> findByFacultyIdIn(List<UUID> facultyCode);

  boolean existsByClassCode(String classCode);

  Page<Classroom> findAll(Specification<Classroom> specs, Pageable pageable);
}
