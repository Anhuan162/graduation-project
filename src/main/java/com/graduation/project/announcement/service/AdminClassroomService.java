package com.graduation.project.announcement.service;

import com.graduation.project.announcement.dto.ClassroomResponse;
import com.graduation.project.announcement.dto.CreatedClassroomRequest;
import com.graduation.project.announcement.dto.FilterClassroomResponse;
import com.graduation.project.announcement.dto.UpdatedClassroomRequest;
import com.graduation.project.announcement.entity.Classroom;
import com.graduation.project.announcement.entity.Faculty;
import com.graduation.project.announcement.repository.ClassroomRepository;
import com.graduation.project.announcement.repository.FacultyRepository;
import com.graduation.project.auth.repository.UserRepository;
import com.graduation.project.cpa.constant.CohortCode;
import com.graduation.project.security.exception.AppException;
import com.graduation.project.security.exception.ErrorCode;
import java.util.List;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class AdminClassroomService {
  private final ClassroomRepository classroomRepository;
  private final FacultyRepository facultyRepository;
  private final UserRepository userRepository;

  public ClassroomResponse createClassroom(CreatedClassroomRequest request, UUID facultyId) {
    if (classroomRepository.existsByClassCode(request.getClassCode())) {
      throw new AppException(ErrorCode.CLASS_CODE_EXISTED);
    }

    Faculty faculty =
        facultyRepository
            .findById(facultyId)
            .orElseThrow(() -> new AppException(ErrorCode.FACULTY_NOT_FOUND));

    Classroom classroom = new Classroom();
    classroom.setClassName(request.getClassName());
    classroom.setClassCode(request.getClassCode());
    classroom.setFaculty(faculty);
    classroom.setStartedYear(request.getStartedYear());
    classroom.setEndedYear(request.getEndedYear());
    classroom.setSchoolYearCode(request.getSchoolYearCode());

    classroomRepository.save(classroom);

    return mapToResponse(classroom);
  }

  public ClassroomResponse updateClassroom(String classroomId, UpdatedClassroomRequest request) {
    Classroom classroom =
        classroomRepository
            .findById(UUID.fromString(classroomId))
            .orElseThrow(() -> new IllegalArgumentException("Classroom not found"));

    classroom.setClassName(request.getClassName());
    classroom.setStartedYear(request.getStartedYear());
    classroom.setEndedYear(request.getEndedYear());
    classroom.setSchoolYearCode(request.getSchoolYearCode());

    classroomRepository.save(classroom);

    return mapToResponse(classroom);
  }

  public Page<ClassroomResponse> searchAllClassrooms(
      String classCode, UUID facultyId, CohortCode schoolYearCode, Pageable pageable) {
    return classroomRepository
        .searchClassrooms(classCode, facultyId, schoolYearCode, pageable)
        .map(this::mapToResponse);
  }

  public List<FilterClassroomResponse> getClassroomsForFilter(
      UUID facultyId, CohortCode schoolYearCode) {
    return classroomRepository
        .findAllByFaculty_IdAndSchoolYearCode(facultyId, schoolYearCode)
        .stream()
        .map(
            c ->
                FilterClassroomResponse.builder()
                    .className(c.getClassName())
                    .classCode(c.getClassCode())
                    .build())
        .toList();
  }

  public void deleteClassroom(String classroomId) {
    Classroom classroom =
        classroomRepository
            .findById(UUID.fromString(classroomId))
            .orElseThrow(() -> new IllegalArgumentException("Classroom not found"));
    classroomRepository.delete(classroom);
  }

  private ClassroomResponse mapToResponse(Classroom c) {
    return ClassroomResponse.builder()
        .id(c.getId().toString())
        .className(c.getClassName())
        .classCode(c.getClassCode())
        .facultyName(c.getFaculty() != null ? c.getFaculty().getFacultyName() : null)
        .startedYear(c.getStartedYear())
        .endedYear(c.getEndedYear())
        .schoolYearCode(c.getSchoolYearCode())
        .build();
  }

  //  @PreAuthorize("hasPermission()")
  //  public Page<Classroom> search(Map<String, String> params, Pageable pageable, User user) {
  //
  //    Specification<Classroom> specs = getSpecification(params, user);
  //    return classroomRepository.findAll(specs, pageable);
  //  }

  //  private Specification<Classroom> getSpecification(Map<String, String> params, User
  // currentUser) {
  //    return (root, criteriaQuery, criteriaBuilder) -> {
  //      List<Predicate> predicateList = new ArrayList<>();
  //
  //      for (Map.Entry<String, String> p : params.entrySet()) {
  //        String key = p.getKey();
  //        String value = p.getValue();
  //
  //        if (!List.of("page", "size", "sort").contains(key.toLowerCase())) {
  //          if ("startCreatedDate".equalsIgnoreCase(key)) {
  //            predicateList.add(
  //                criteriaBuilder.greaterThanOrEqualTo(
  //                    root.get("createdDate"),
  //                    LocalDate.parse(value, DateTimeFormatter.ofPattern("yyyy-MM-dd"))
  //                        .atStartOfDay()
  //                        .toInstant(ZoneOffset.UTC)));
  //          } else if ("endCreatedDate".equalsIgnoreCase(key)) {
  //            predicateList.add(
  //                criteriaBuilder.lessThanOrEqualTo(
  //                    root.get("createdDate"),
  //                    LocalDate.parse(value, DateTimeFormatter.ofPattern("yyyy-MM-dd"))
  //                        .atStartOfDay()
  //                        .toInstant(ZoneOffset.UTC)));
  //          } else {
  //            if (value != null && (value.contains("*") || value.contains("%"))) {
  //              predicateList.add(
  //                  criteriaBuilder.like(root.get(key), "%" + value.replace("*", "%") + "%"));
  //            } else if (value != null) {
  //              predicateList.add(criteriaBuilder.like(root.get(key), value + "%"));
  //            }
  //          }
  //        }
  //      }
  //
  //      if (currentUser != null) {
  //        predicateList.add(criteriaBuilder.equal(root.get("teacher"), currentUser.getId()));
  //      }
  //
  //      return predicateList.isEmpty()
  //          ? criteriaBuilder.conjunction()
  //          : criteriaBuilder.and(predicateList.toArray(new Predicate[0]));
  //    };
  //  }
}
