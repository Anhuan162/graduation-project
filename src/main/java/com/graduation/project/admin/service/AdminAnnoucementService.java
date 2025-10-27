package com.graduation.project.admin.service;

import com.graduation.project.admin.dto.AnnouncementResponse;
import com.graduation.project.admin.dto.CreatedAnnoucementResponse;
import com.graduation.project.admin.dto.CreatedAnnouncementRequest;
import com.graduation.project.admin.dto.UpdatedAnnoucementRequest;
import com.graduation.project.auth.exception.AppException;
import com.graduation.project.auth.exception.ErrorCode;
import com.graduation.project.auth.repository.UserRepository;
import com.graduation.project.common.entity.*;
import com.graduation.project.common.repository.AnnoucementRepository;
import com.graduation.project.common.repository.AnnoucementTargetRepository;
import com.graduation.project.common.repository.ClassroomRepository;
import com.graduation.project.notification.NotificationMessageDTO;
import com.graduation.project.notification.NotificationStreamProducer;
import jakarta.transaction.Transactional;
import java.time.LocalDate;
import java.time.LocalDateTime;
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
  private final NotificationStreamProducer producer;
  private final UserRepository userRepository;

  public CreatedAnnoucementResponse createAnnouncement(
      CreatedAnnouncementRequest request, User user) {
    Annoucement announcement = CreatedAnnouncementRequest.toAnnoucement(request, user);

    Set<String> allClassroomCodes =
        getAllClassroomCodes(
            request.getSchoolYearCodes(), request.getFacultyIds(), request.getClassCodes());

    List<AnnoucementTarget> announcementTargets =
        generateAnnoucementTargets(allClassroomCodes, announcement);

    announcement.setTargets(announcementTargets);
    annoucementRepository.save(announcement);

    List<User> receivedUsers = userRepository.findAllByRoleName("USER");
    List<String> receiverUserIds = receivedUsers.stream().map(re -> re.getId().toString()).toList();
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
    producer.publish(dto);

    return CreatedAnnoucementResponse.from(announcement);
  }

  private static List<AnnoucementTarget> generateAnnoucementTargets(
      Set<String> allClassroomCodes, Annoucement announcement) {
    return allClassroomCodes.stream()
        .map(
            classroomCode -> {
              return AnnoucementTarget.builder()
                  .classroomCode(classroomCode)
                  .id(UUID.randomUUID())
                  .annoucement(announcement)
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
      String announcementId, UpdatedAnnoucementRequest request, User user) {
    Annoucement annoucement =
        annoucementRepository
            .findById(announcementId)
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
    return AnnouncementResponse.from(annoucement);
  }

  public AnnouncementResponse getAnnouncement(String annoucementId) {
    var announcement =
        annoucementRepository
            .findById(annoucementId)
            .orElseThrow(() -> new AppException(ErrorCode.ANNOUCEMENT_NOT_FOUND));
    return AnnouncementResponse.from(announcement);
  }

  public List<AnnouncementResponse> getAnnouncements() {
    return AnnouncementResponse.from(annoucementRepository.findAll());
  }
}
