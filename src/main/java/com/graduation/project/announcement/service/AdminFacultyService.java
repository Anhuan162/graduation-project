package com.graduation.project.announcement.service;

import com.graduation.project.announcement.dto.CreatedFacultyRequest;
import com.graduation.project.announcement.dto.FacultyResponse;
import com.graduation.project.announcement.dto.UpdatedFacultyRequest;
import com.graduation.project.announcement.entity.Faculty;
import com.graduation.project.announcement.repository.FacultyRepository;
import com.graduation.project.security.exception.AppException;
import com.graduation.project.security.exception.ErrorCode;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class AdminFacultyService {
  private final FacultyRepository facultyRepository;

  public FacultyResponse createFaculty(CreatedFacultyRequest request) {
    if (facultyRepository.existsByFacultyCode(request.getFacultyCode())) {
      throw new AppException(ErrorCode.FACULTY_EXISTED);
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
    faculty.setFacultyCode(request.getFacultyCode());
    faculty.setDescription(request.getDescription());

    facultyRepository.save(faculty);
    return mapToResponse(faculty);
  }

  public Page<FacultyResponse> getAllFaculties(Pageable pageable) {
    return facultyRepository.findAll(pageable).map(this::mapToResponse);
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
