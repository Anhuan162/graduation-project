package com.graduation.project.announcement.service;

import com.graduation.project.announcement.dto.*;
import com.graduation.project.announcement.entity.Announcement;
import com.graduation.project.announcement.entity.AnnouncementTarget;
import com.graduation.project.announcement.entity.Classroom;
import com.graduation.project.announcement.repository.AnnouncementRepository;
import com.graduation.project.announcement.repository.ClassroomRepository;
import com.graduation.project.auth.repository.UserRepository;
import com.graduation.project.auth.service.CurrentUserService;
import com.graduation.project.common.constant.ResourceType;
import com.graduation.project.common.entity.FileMetadata;
import com.graduation.project.common.entity.User;
import com.graduation.project.common.service.FileService;
import com.graduation.project.cpa.constant.CohortCode;
import com.graduation.project.event.dto.NotificationEventDTO;
import com.graduation.project.security.exception.AppException;
import com.graduation.project.security.exception.ErrorCode;
import jakarta.persistence.criteria.Predicate;
import jakarta.transaction.Transactional;

import java.time.Instant;
import java.time.LocalDate;
import java.util.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;

@RequiredArgsConstructor
@Log4j2
@Service
@Transactional
public class AdminAnnouncementService {
  private static final String CREATED_DATE = "createdDate";

  private final AnnouncementRepository announcementRepository;
  private final ClassroomRepository classroomRepository;
  private final CurrentUserService currentUserService;
  private final ApplicationEventPublisher publisher;
  private final FileService fileService;
  private final UserRepository userRepository;

  @Transactional
  public CreatedAnnonucementResponse createAnnouncement(CreatedAnnouncementRequest request, User user) {
    Announcement announcement = CreatedAnnouncementRequest.toAnnouncement(request, user);
    announcementRepository.save(announcement);

    List<FileMetadata> fileMetadataList = fileService.updateFileMetadataList(
        request.getFileMetadataIds(),
        announcement.getId(),
        ResourceType.ANNOUNCEMENT,
        user.getId());

    List<String> urls = fileMetadataList.stream().map(FileMetadata::getUrl).toList();
    return CreatedAnnonucementResponse.from(announcement, urls);
  }

  public void releaseAnnouncement(UUID announcementId, ReleaseAnnouncementRequest request) {
    Announcement announcement = announcementRepository.findById(announcementId).orElseThrow(
        () -> new AppException(ErrorCode.ANNOUNCEMENT_NOT_FOUND, "Announcement not found for id: " + announcementId));

    if (Boolean.TRUE.equals(announcement.getAnnouncementStatus())) {
      throw new AppException(ErrorCode.CONFLICT, "Thông báo đã được gửi");
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
        .createdAt(Instant.now())
        .receiverIds(receiverUserIds)
        .build();

    publisher.publishEvent(notificationEventDTO);
  }

  private static List<AnnouncementTarget> generateAnnouncementTargets(Set<String> allClassroomCodes,
      Announcement announcement) {
    return allClassroomCodes.stream()
        .map(classroomCode -> AnnouncementTarget.builder()
            .classroomCode(classroomCode)
            .announcement(announcement)
            .build())
        .toList();
  }

  private Set<String> getAllClassroomCodes(List<CohortCode> schoolYearCodes, List<UUID> facultyIds,
      List<String> classCodes) {
    Set<String> allClassroomCodes = new HashSet<>();

    if (schoolYearCodes != null) {
      List<String> codes = classroomRepository.findBySchoolYearCodeIn(schoolYearCodes).stream()
          .map(Classroom::getClassCode)
          .toList();
      allClassroomCodes.addAll(codes);
    }

    if (facultyIds != null) {
      List<String> codes = classroomRepository.findByFacultyIdIn(facultyIds).stream()
          .map(Classroom::getClassCode)
          .toList();
      allClassroomCodes.addAll(codes);
    }

    if (classCodes != null) {
      allClassroomCodes.addAll(classCodes);
    }

    return allClassroomCodes;
  }

  public AnnouncementResponse updateAnnouncement(String announcementId, UpdatedAnnouncementRequest request, User user) {
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

    announcement.setTargets(generateAnnouncementTargets(allClassroomCodes, announcement));
    announcementRepository.save(announcement);
    return AnnouncementResponse.from(announcement);
  }

  public AnnouncementResponse getAnnouncement(String announcementId) {
    Announcement announcement = announcementRepository
        .findById(UUID.fromString(announcementId))
        .orElseThrow(() -> new AppException(ErrorCode.ANNOUNCEMENT_NOT_FOUND));
    return AnnouncementResponse.from(announcement);
  }

  public Page<AnnouncementResponse> searchAnnouncement(SearchAnnouncementRequest request, Pageable pageable) {
    Specification<Announcement> specification = (root, query, cb) -> {
      List<Predicate> predicates = new ArrayList<>();

      if (request.getTitle() != null && !request.getTitle().isBlank()) {
        predicates.add(cb.like(
            cb.lower(root.get("title")),
            "%" + request.getTitle().toLowerCase() + "%"));
      }

      if (request.getAnnouncementType() != null) {
        predicates.add(cb.equal(root.get("announcementType"), request.getAnnouncementType()));
      }

      if (request.getAnnouncementStatus() != null) {
        predicates.add(cb.equal(root.get("announcementStatus"), request.getAnnouncementStatus()));
      }

      if (request.getFromDate() != null) {
        predicates.add(cb.greaterThanOrEqualTo(root.get(CREATED_DATE), request.getFromDate()));
      }

      if (request.getToDate() != null) {
        predicates.add(cb.lessThanOrEqualTo(root.get(CREATED_DATE), request.getToDate()));
      }

      Objects.requireNonNull(query).orderBy(cb.desc(root.get(CREATED_DATE)));
      return cb.and(predicates.toArray(new Predicate[0]));
    };

    return announcementRepository.findAll(specification, pageable).map(AnnouncementResponse::from);
  }

  public void deleteAnnouncement(String announcementId) {
    Announcement announcement = announcementRepository
        .findById(UUID.fromString(announcementId))
        .orElseThrow(() -> new AppException(ErrorCode.ANNOUNCEMENT_NOT_FOUND));
    announcementRepository.delete(announcement);
  }
}
