package com.graduation.project.announcement.repository;

import com.graduation.project.announcement.entity.Faculty;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface FacultyRepository extends JpaRepository<Faculty, UUID> {
  boolean existsByFacultyCode(String facultyCode);

  Optional<Faculty> findByFacultyCode(String facultyCode);

  Page<Faculty> findByFacultyNameContainingIgnoreCaseOrFacultyCodeContainingIgnoreCase(
      String facultyName, String facultyCode, Pageable pageable);
}
