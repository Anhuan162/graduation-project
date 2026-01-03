package com.graduation.project.announcement.service;

import com.graduation.project.announcement.constant.AnnouncementType;
import com.graduation.project.announcement.dto.AnnouncementResponse;
import com.graduation.project.announcement.dto.FullAnnouncementResponse;
import com.graduation.project.announcement.entity.Announcement;
import com.graduation.project.announcement.mapper.AnnouncementMapper;
import com.graduation.project.announcement.repository.AnnouncementRepository;
import com.graduation.project.announcement.specification.AnnouncementSpecification;
import com.graduation.project.auth.service.CurrentUserService;
import com.graduation.project.common.constant.ResourceType;
import com.graduation.project.common.entity.User;
import com.graduation.project.common.service.FileService;
import com.graduation.project.security.exception.AppException;
import com.graduation.project.security.exception.ErrorCode;
import java.time.LocalDate;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;

@RequiredArgsConstructor
@Service
@Log4j2
public class AnnouncementService {

  private final AnnouncementRepository announcementRepository;
  private final AnnouncementMapper announcementMapper;
  private final CurrentUserService currentUserService;
  private final FileService fileService;

  public Page<AnnouncementResponse> searchAnnouncements(
      Pageable pageable,
      AnnouncementType type,
      String keyword,
      Boolean status,
      LocalDate fromDate,
      LocalDate toDate) {
    Boolean effectiveStatus = (status == null) ? Boolean.TRUE : status;
    User currentUser = currentUserService.getCurrentUserEntity();
    if (currentUser == null) {
      throw new IllegalStateException("Current user entity is null");
    }
    String classroomCode = currentUser.getClassCode();

    Specification<Announcement> spec = Specification
        .where(AnnouncementSpecification.withType(type))
        .and(AnnouncementSpecification.keyword(keyword))
        .and(AnnouncementSpecification.withStatus(effectiveStatus))
        .and(AnnouncementSpecification.fromDate(fromDate))
        .and(AnnouncementSpecification.toDate(toDate))
        .and(AnnouncementSpecification.withClassroomCode(classroomCode));

    return announcementRepository.findAll(spec, pageable)
        .map(announcementMapper::toResponse);
  }

  public AnnouncementResponse getDetail(String announcementId) {
    UUID uuid;
    try {
      uuid = UUID.fromString(announcementId);
    } catch (IllegalArgumentException e) {
      throw new AppException(ErrorCode.UUID_IS_INVALID);
    }

    Announcement entity = announcementRepository.findById(uuid)
        .orElseThrow(() -> new AppException(ErrorCode.ANNOUNCEMENT_NOT_FOUND));

    return announcementMapper.toResponse(entity);
  }

  public FullAnnouncementResponse getAnnouncementDetail(UUID announcementId) {
    Announcement entity = announcementRepository.findById(announcementId)
        .orElseThrow(() -> new AppException(ErrorCode.ANNOUNCEMENT_NOT_FOUND));

    FullAnnouncementResponse response = announcementMapper.toFullResponse(entity);
    response.setAttachments(fileService.getFilesByResource(announcementId, ResourceType.ANNOUNCEMENT));

    return response;
  }
}
