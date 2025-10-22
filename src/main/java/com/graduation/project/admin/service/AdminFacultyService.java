package com.graduation.project.admin.service;

import com.graduation.project.admin.dto.*;
import com.graduation.project.common.entity.Faculty;
import com.graduation.project.common.repository.FacultyRepository;
import java.util.List;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class AdminFacultyService {
  private final FacultyRepository facultyRepository;

  public FacultyResponse createFaculty(CreatedFacultyRequest request) {
    if (facultyRepository.existsByFacultyCode(request.getFacultyCode())) {
      throw new IllegalArgumentException("Faculty code already exists");
    }

    Faculty faculty = new Faculty();
    faculty.setFacultyName(request.getFacultyName());
    faculty.setFacultyCode(request.getFacultyCode());
    faculty.setDescription(request.getDescription());

    facultyRepository.save(faculty);

    return mapToResponse(faculty);
  }

  public FacultyResponse updateFaculty(String facultyId, UpdatedFacultyRequest request) {
    Faculty faculty =
        facultyRepository
            .findById(UUID.fromString(facultyId))
            .orElseThrow(() -> new IllegalArgumentException("Faculty not found"));

    faculty.setFacultyName(request.getFacultyName());
    faculty.setDescription(request.getDescription());

    facultyRepository.save(faculty);
    return mapToResponse(faculty);
  }

  public List<FacultyResponse> getAllFaculties() {
    return facultyRepository.findAll().stream().map(this::mapToResponse).toList();
  }

  public void deleteFaculty(String facultyId) {
    Faculty faculty =
        facultyRepository
            .findById(UUID.fromString(facultyId))
            .orElseThrow(() -> new IllegalArgumentException("Faculty not found"));
    facultyRepository.delete(faculty);
  }

  private FacultyResponse mapToResponse(Faculty f) {
    return FacultyResponse.builder()
        .id(f.getId().toString())
        .facultyName(f.getFacultyName())
        .facultyCode(f.getFacultyCode())
        .description(f.getDescription())
        .build();
  }
}
