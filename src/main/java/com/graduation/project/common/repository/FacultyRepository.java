package com.graduation.project.common.repository;

import com.graduation.project.common.entity.Faculty;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface FacultyRepository extends JpaRepository<Faculty, UUID> {
  boolean existsByFacultyCode(String facultyCode);

  Faculty findByFacultyCode(String facultyCode);
}
