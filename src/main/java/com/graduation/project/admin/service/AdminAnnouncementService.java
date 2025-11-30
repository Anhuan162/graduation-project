package com.graduation.project.admin.service;

import com.graduation.project.admin.dto.AnnouncementResponse;
import com.graduation.project.admin.dto.CreatedAnnonucementResponse;
import com.graduation.project.admin.dto.CreatedAnnouncementRequest;
import com.graduation.project.admin.dto.UpdatedAnnouncementRequest;
import com.graduation.project.auth.exception.AppException;
import com.graduation.project.auth.exception.ErrorCode;
import com.graduation.project.auth.repository.UserRepository;
import com.graduation.project.common.entity.*;
import com.graduation.project.common.repository.AnnouncementRepository;
import com.graduation.project.common.repository.AnnouncementTargetRepository;
import com.graduation.project.common.repository.ClassroomRepository;
import com.graduation.project.event.dto.EventEnvelope;
import com.graduation.project.event.dto.NotificationMessageDTO;
import com.graduation.project.event.producer.StreamProducer;
import jakarta.transaction.Transactional;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

@RequiredArgsConstructor
@Log4j2
@Service
@Transactional
public class AdminAnnouncementService {
  private final AnnouncementRepository announcementRepository;
  private final AnnouncementTargetRepository announcementTargetRepository;
  private final ClassroomRepository classroomRepository;
  private final StreamProducer producer;
  private final UserRepository userRepository;

  public CreatedAnnonucementResponse createAnnouncement(
      CreatedAnnouncementRequest request, User user) {
    Announcement announcement = CreatedAnnouncementRequest.toAnnouncement(request, user);

    Set<String> allClassroomCodes =
        getAllClassroomCodes(
            request.getSchoolYearCodes(), request.getFacultyIds(), request.getClassCodes());

    List<AnnouncementTarget> announcementTargets =
        generateAnnouncementTargets(allClassroomCodes, announcement);

    announcement.setTargets(announcementTargets);
    announcementRepository.save(announcement);

    List<User> receivedUsers = userRepository.findAllByRoleName("USER");
    List<UUID> receiverUserIds = receivedUsers.stream().map(User::getId).toList();
    NotificationMessageDTO dto =
        NotificationMessageDTO.builder()
            .relatedId(announcement.getId())
            .type(NotificationType.ANNOUNCEMENT)
            .title(announcement.getTitle())
            .content(announcement.getContent())
            .senderId(user.getId())
            .senderName(user.getEmail())
            .receiverIds(receiverUserIds) // build list of UUID receivers
            .createdAt(LocalDateTime.now())
            .build();
    EventEnvelope eventEnvelope = EventEnvelope.from(EventType.NOTIFICATION, dto, "ANNOUNCEMENT");
    producer.publish(eventEnvelope);

    return CreatedAnnonucementResponse.from(announcement);
  }

  private static List<AnnouncementTarget> generateAnnouncementTargets(
      Set<String> allClassroomCodes, Announcement announcement) {
    return allClassroomCodes.stream()
        .map(
            classroomCode -> {
              return AnnouncementTarget.builder()
                  .classroomCode(classroomCode)
                  .id(UUID.randomUUID())
                  .announcement(announcement)
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

  public AnnouncementResponse updateAnnouncement(
      String announcementId, UpdatedAnnouncementRequest request, User user) {
    Announcement announcement =
        announcementRepository
            .findById(announcementId)
            .orElseThrow(() -> new AppException(ErrorCode.ANNOUNCEMENT_NOT_FOUND));

    announcement.setTitle(request.getTitle());
    announcement.setContent(request.getContent());
    announcement.setAnnouncementStatus(request.getAnnouncementStatus());
    announcement.setModifiedBy(user);
    announcement.setModifiedDate(LocalDate.now());

    Set<String> allClassroomCodes =
        getAllClassroomCodes(
            request.getSchoolYearCodes(), request.getFacultyIds(), request.getClassCodes());
    announcement.setTargets(generateAnnouncementTargets(allClassroomCodes, announcement));
    announcementRepository.save(announcement);
    return AnnouncementResponse.from(announcement);
  }

  public AnnouncementResponse getAnnouncement(String announcementId) {
    var announcement =
        announcementRepository
            .findById(announcementId)
            .orElseThrow(() -> new AppException(ErrorCode.ANNOUNCEMENT_NOT_FOUND));
    return AnnouncementResponse.from(announcement);
  }

//  ThieuNN
  public Page<AnnouncementResponse> getAnnouncements(Integer pageNumber, Integer pageSize) {
    Pageable pageable = PageRequest.of(pageNumber, pageSize);
    Page<Announcement> announcementPage = announcementRepository.findAll(pageable);
    return announcementPage.map(AnnouncementResponse::from);
  }
}
