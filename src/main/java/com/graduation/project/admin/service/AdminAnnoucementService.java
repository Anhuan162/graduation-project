package com.graduation.project.admin.service;

import com.graduation.project.admin.dto.CreatedAnnoucementRequest;
import com.graduation.project.admin.dto.CreatedAnnoucementResponse;
import com.graduation.project.admin.dto.UpdatedAnnoucementRequest;
import com.graduation.project.admin.dto.UpdatedAnnoucementResponse;
import com.graduation.project.auth.exception.AppException;
import com.graduation.project.auth.exception.ErrorCode;
import com.graduation.project.common.entity.Annoucement;
import com.graduation.project.common.entity.AnnoucementTarget;
import com.graduation.project.common.entity.Classroom;
import com.graduation.project.common.entity.User;
import com.graduation.project.common.repository.AnnoucementRepository;
import com.graduation.project.common.repository.AnnoucementTargetRepository;
import com.graduation.project.common.repository.ClassroomRepository;
import jakarta.transaction.Transactional;
import java.time.LocalDate;
import java.util.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.springframework.stereotype.Service;

@RequiredArgsConstructor
@Log4j2
@Service
@Transactional
public class AdminAnnoucementService {
  private final AnnoucementRepository annoucementRepository;
  private final AnnoucementTargetRepository annoucementTargetRepository;
  private final ClassroomRepository classroomRepository;

  public CreatedAnnoucementResponse createAnnoucement(
      CreatedAnnoucementRequest request, User user) {
    Annoucement annoucement = CreatedAnnoucementRequest.toAnnoucement(request, user);

    Set<String> allClassroomCodes =
        getAllClassroomCodes(
            request.getSchoolYearCodes(), request.getFacultyIds(), request.getClassCodes());

    List<AnnoucementTarget> annoucementTargets =
        generateAnnoucementTargets(allClassroomCodes, annoucement);

    annoucement.setTargets(annoucementTargets);
    annoucementRepository.save(annoucement);
    return CreatedAnnoucementResponse.from(annoucement);
  }

  private static List<AnnoucementTarget> generateAnnoucementTargets(
      Set<String> allClassroomCodes, Annoucement annoucement) {
    return allClassroomCodes.stream()
        .map(
            classroomCode -> {
              return AnnoucementTarget.builder()
                  .classroomCode(classroomCode)
                  .id(UUID.randomUUID())
                  .annoucement(annoucement)
                  .build();
            })
        .toList();
  }

  private Set<String> getAllClassroomCodes(
      List<String> schoolYearCodes, List<UUID> facultyIds, List<String> classCodes) {
    Set<String> allClassroomCodes = new HashSet<>();
    if (Objects.nonNull(schoolYearCodes)) {
      List<String> classroomCodeBySchoolYearCodes =
          classroomRepository.findBySchoolYearCodeIn(schoolYearCodes).stream()
              .map(Classroom::getClassCode)
              .toList();
      allClassroomCodes.addAll(classroomCodeBySchoolYearCodes);
    }

    if (Objects.nonNull(facultyIds)) {
      List<String> classroomCodeByFacultyCodes =
          classroomRepository.findByFacultyIdIn(facultyIds).stream()
              .map(Classroom::getClassCode)
              .toList();
      allClassroomCodes.addAll(classroomCodeByFacultyCodes);
    }

    if (Objects.nonNull(classCodes)) {
      allClassroomCodes.addAll(classCodes);
    }
    return allClassroomCodes;
  }

  public UpdatedAnnoucementResponse updateAnnoucement(
      String annoucementId, UpdatedAnnoucementRequest request, User user) {
    Annoucement annoucement =
        annoucementRepository
            .findById(annoucementId)
            .orElseThrow(() -> new AppException(ErrorCode.ANNOUCEMENT_NOT_FOUND));

    annoucement.setTitle(request.getTitle());
    annoucement.setContent(request.getContent());
    annoucement.setAnnoucementStatus(request.getAnnoucementStatus());
    annoucement.setModifiedBy(user);
    annoucement.setModifiedDate(LocalDate.now());

    Set<String> allClassroomCodes =
        getAllClassroomCodes(
            request.getSchoolYearCodes(), request.getFacultyIds(), request.getClassCodes());
    annoucement.setTargets(generateAnnoucementTargets(allClassroomCodes, annoucement));
    annoucementRepository.save(annoucement);
    return UpdatedAnnoucementResponse.from(annoucement);
  }
}
