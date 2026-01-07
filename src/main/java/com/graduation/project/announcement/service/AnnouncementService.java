package com.graduation.project.announcement.service;

import com.graduation.project.announcement.dto.*;
import com.graduation.project.announcement.entity.Announcement;
import com.graduation.project.announcement.entity.AnnouncementTarget;
import com.graduation.project.announcement.entity.Classroom;
import com.graduation.project.announcement.mapper.AnnouncementMapper;
import com.graduation.project.announcement.repository.AnnouncementRepository;
import com.graduation.project.announcement.repository.AnnouncementTargetRepository;
import com.graduation.project.announcement.repository.ClassroomRepository;
import com.graduation.project.auth.repository.UserRepository;
import com.graduation.project.auth.service.CurrentUserService;
import com.graduation.project.common.constant.ResourceType;
import com.graduation.project.common.dto.FileResponse;
import com.graduation.project.common.entity.FileMetadata;
import com.graduation.project.common.entity.User;
import com.graduation.project.common.service.DriveService;
import com.graduation.project.common.service.FileService;
import com.graduation.project.cpa.constant.CohortCode;
import com.graduation.project.event.dto.NotificationEventDTO;
import com.graduation.project.security.exception.AppException;
import com.graduation.project.security.exception.ErrorCode;
import jakarta.persistence.criteria.Predicate;
import jakarta.transaction.Transactional;

import java.io.IOException;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.checkerframework.checker.units.qual.A;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;

@RequiredArgsConstructor
@Log4j2
@Service
@Transactional
public class AnnouncementService {
  private final AnnouncementRepository announcementRepository;
  private final ClassroomRepository classroomRepository;
  private final AnnouncementTargetRepository announcementTargetRepository;
  private final CurrentUserService currentUserService;
  private final ApplicationEventPublisher publisher;
  private final FileService fileService;
  private final UserRepository userRepository;
  private final AnnouncementMapper announcementMapper;
  private final DriveService driveService;

  @Transactional
  public AnnouncementResponse createAnnouncement(CreateAnnouncementRequest request, User user) {
    // 1. Save Announcement (Parent)
    Announcement announcement = Announcement.builder()
        .title(request.getTitle())
        .content(request.getContent())
        .announcementType(request.getAnnouncementType()) // Set type
        .createdBy(user)
        .createdDate(LocalDate.now())
        .announcementStatus(true)
        .build();
    announcementRepository.save(announcement);

    List<UUID> fileMetadataIds = new ArrayList<>();
    if (request.getFileMetadataIds() != null) {
      fileMetadataIds = request.getFileMetadataIds().stream()
          .map(UUID::fromString)
          .toList();
    }

    fileService.updateFileMetadataList(
        fileMetadataIds,
        announcement.getId(),
        ResourceType.ANNOUNCEMENT,
        user.getId());

    // 2. Resolve Targets (Data Explosion)
    Set<String> classCodesToSave = new HashSet<>();

    if (Boolean.TRUE.equals(request.getIsGlobal())) {
      classCodesToSave.addAll(classroomRepository.findAllClassCodes());
    } else {
      if (!org.springframework.util.CollectionUtils.isEmpty(request.getTargetFaculties())) {
        List<UUID> facultyIds = request.getTargetFaculties().stream()
            .map(UUID::fromString)
            .toList();
        classCodesToSave.addAll(classroomRepository.findClassCodesByFacultyIdIn(facultyIds));
      }

      // Cohort Targets
      if (!org.springframework.util.CollectionUtils.isEmpty(request.getTargetCohorts())) {
        List<CohortCode> cohorts = request.getTargetCohorts().stream()
            .map(CohortCode::valueOf)
            .toList();
        classCodesToSave.addAll(classroomRepository.findClassCodesBySchoolYearCodeIn(cohorts));
      }

      // Specific Class Codes
      if (!org.springframework.util.CollectionUtils.isEmpty(request.getSpecificClassCodes())) {
        classCodesToSave.addAll(request.getSpecificClassCodes());
      }
    }

    // 3. Batch Persistence with Chunking (Memory Optimization)
    List<String> allCodesList = new ArrayList<>(classCodesToSave);
    // Guava Lists.partition to split large list into smaller chunks
    List<List<String>> chunks = com.google.common.collect.Lists.partition(allCodesList, 500);

    for (List<String> chunk : chunks) {
      List<AnnouncementTarget> targets = chunk.stream()
          .map(code -> AnnouncementTarget.builder()
              .announcement(announcement)
              .classroomCode(code)
              .build())
          .toList();

      announcementTargetRepository.saveAll(targets);
      // Optional: Flush to send statements to DB and clear memory if needed (though
      // transaction handles commit at end)
      // announcementTargetRepository.flush();
      // announcementTargetRepository.flush();
    }
    return AnnouncementResponse.from(announcement);
  }

  public void releaseAnnouncement(UUID announcementId, ReleaseAnnouncementRequest request) {
    Announcement announcement = announcementRepository.findById(announcementId).orElseThrow();

    if (announcement.getAnnouncementStatus()) {
      throw new RuntimeException("Thông báo đã được gửi");
    }

    Set<String> allClassroomCodes = getAllClassroomCodes(
        request.getSchoolYearCodes(), request.getFacultyIds(), request.getClassCodes());

    List<AnnouncementTarget> announcementTargets = generateAnnouncementTargets(allClassroomCodes, announcement);
    announcement.getTargets().addAll(announcementTargets);
    announcement.setAnnouncementStatus(true);
    announcementRepository.save(announcement);

    User user = currentUserService.getCurrentUserEntity();
    Set<UUID> receiverUserIds = new HashSet<>(userRepository.findUserIdsByClassCodes(allClassroomCodes));

    NotificationEventDTO notificationEventDTO = NotificationEventDTO.builder()
        .relatedId(announcement.getId())
        .type(ResourceType.ANNOUNCEMENT)
        .title("Thông báo mới " + announcement.getTitle())
        .content(announcement.getContent())
        .senderId(user.getId())
        .senderName(user.getEmail())
        .createdAt(LocalDateTime.now())
        .receiverIds(receiverUserIds)
        .build();

    publisher.publishEvent(notificationEventDTO);
  }

  private static List<AnnouncementTarget> generateAnnouncementTargets(
      Set<String> allClassroomCodes, Announcement announcement) {
    return allClassroomCodes.stream()
        .map(
            classroomCode -> {
              return AnnouncementTarget.builder()
                  .classroomCode(classroomCode)
                  .announcement(announcement)
                  .build();
            })
        .collect(java.util.stream.Collectors.toList());
  }

  private Set<String> getAllClassroomCodes(
      List<CohortCode> schoolYearCodes, List<UUID> facultyIds, List<String> classCodes) {
    Set<String> allClassroomCodes = new HashSet<>();
    if (Objects.nonNull(schoolYearCodes)) {
      List<String> classroomCodeBySchoolYearCodes = classroomRepository.findBySchoolYearCodeIn(schoolYearCodes).stream()
          .map(Classroom::getClassCode)
          .toList();
      allClassroomCodes.addAll(classroomCodeBySchoolYearCodes);
    }

    if (Objects.nonNull(facultyIds)) {
      List<String> classroomCodeByFacultyCodes = classroomRepository.findByFacultyIdIn(facultyIds).stream()
          .map(Classroom::getClassCode)
          .toList();
      allClassroomCodes.addAll(classroomCodeByFacultyCodes);
    }

    if (Objects.nonNull(classCodes)) {
      allClassroomCodes.addAll(classCodes);
    }
    return allClassroomCodes;
  }

  public AnnouncementResponse updateAnnouncement(
      String announcementId, UpdatedAnnouncementRequest request, User user) {
    Announcement announcement = announcementRepository
        .findById(UUID.fromString(announcementId))
        .orElseThrow(() -> new AppException(ErrorCode.ANNOUNCEMENT_NOT_FOUND));

    announcement.setTitle(request.getTitle());
    announcement.setContent(request.getContent());
    announcement.setAnnouncementStatus(request.getAnnouncementStatus());
    announcement.setModifiedBy(user);
    announcement.setModifiedDate(LocalDate.now());

    Set<String> allClassroomCodes = getAllClassroomCodes(
        request.getSchoolYearCodes(), request.getFacultyIds(), request.getClassCodes());

    // Use clear/addAll to respect orphanRemoval and Hibernate managed collection
    if (announcement.getTargets() == null) {
      announcement.setTargets(new ArrayList<>());
    }
    announcement.getTargets().clear();
    announcement.getTargets().addAll(generateAnnouncementTargets(allClassroomCodes, announcement));

    announcementRepository.save(announcement);
    return AnnouncementResponse.from(announcement);
  }

  public DetailedAnnouncementResponse getAnnouncement(UUID announcementId) {
    var announcement = announcementRepository
        .findById(announcementId)
        .orElseThrow(() -> new AppException(ErrorCode.ANNOUNCEMENT_NOT_FOUND));

    List<AnnouncementFileResponse> attachments = fileService
        .findFileMetadataByResourceTarget(announcementId, ResourceType.ANNOUNCEMENT)
        .stream()
        .map(
            file -> AnnouncementFileResponse.builder()
                .id(file.getId())
                .fileName(file.getFileName())
                .url(file.getUrl())
                .fileType(file.getContentType())
                .size(file.getSize())
                .build())
        .toList();

    return DetailedAnnouncementResponse.from(announcement, attachments);
  }

  @Transactional
  public Page<AnnouncementResponse> searchAnnouncement(
      SearchAnnouncementRequest request, Pageable pageable) {
    Specification<Announcement> specification = (root, query, cb) -> {
      List<Predicate> predicates = new ArrayList<>();

      if (Objects.nonNull(request.getTitle())) {
        predicates.add(
            cb.like(root.get("title").as(String.class), "%" + request.getTitle() + "%"));
      }

      if (Objects.nonNull(request.getAnnouncementType())) {
        predicates.add(
            cb.equal(
                root.get("announcementType").as(String.class), request.getAnnouncementType()));
      }

      if (Objects.nonNull(request.getAnnouncementStatus())) {
        predicates.add(
            cb.equal(root.get("announcementStatus"), request.getAnnouncementStatus()));
      }

      if (Objects.nonNull(request.getAnnouncementProvider())) {
        predicates.add(
            cb.equal(root.get("announcementProvider"), request.getAnnouncementProvider()));
      }

      if (Objects.nonNull(request.getFromDate())) {
        predicates.add(
            cb.greaterThanOrEqualTo(
                root.get("createdDate"), request.getFromDate().atStartOfDay()));
      }

      if (Objects.nonNull(request.getToDate())) {
        predicates.add(
            cb.lessThanOrEqualTo(
                root.get("createdDate"), request.getToDate().atTime(23, 59, 59)));
      }

      Objects.requireNonNull(query).orderBy(cb.desc(root.get("createdDate")));

      return cb.and(predicates.toArray(new Predicate[0]));
    };

    Page<Announcement> announcementPage = announcementRepository.findAll(specification, pageable);
    return announcementPage.map(AnnouncementResponse::from);
  }

  public void deleteAnnouncement(String announcementId) {
    Announcement announcement = announcementRepository
        .findById(UUID.fromString(announcementId))
        .orElseThrow(() -> new AppException(ErrorCode.ANNOUNCEMENT_NOT_FOUND));

    announcementRepository.delete(announcement);
  }

  @Transactional
  public Page<AnnouncementResponse> searchActiveAnnouncements(
      SearchActiveAnnouncementRequest request, Pageable pageable) {
    Page<Announcement> announcementPage = announcementRepository.findAllActiveAnnouncements(request, pageable);
    return announcementPage.map(announcementMapper::toResponse);
  }

  public FileResponse addAnnouncementToDrive(String announcementId) throws IOException {
    UUID id = null;
    try {
      id = UUID.fromString(announcementId);
    } catch (Exception e) {
      throw new AppException(ErrorCode.UUID_IS_INVALID);
    }
    Optional<Announcement> announcement = announcementRepository.findById(id);
    if (announcement.isEmpty()) {
      throw new AppException(ErrorCode.ANNOUNCEMENT_NOT_FOUND);
    }
    FileResponse FileResponse = driveService.uploadTextToDrive(announcement.get().getTitle(),
        announcement.get().getContent());
    return FileResponse;
  }
}
