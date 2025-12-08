package com.graduation.project.announcement.service;

import com.graduation.project.announcement.dto.AnnouncementResponse;
import com.graduation.project.announcement.entity.Announcement;
import com.graduation.project.announcement.mapper.AnnouncementMapper;
import com.graduation.project.announcement.repository.AnnouncementRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

@RequiredArgsConstructor
@Service
@Log4j2
public class AnnouncementService {
  private final AnnouncementRepository announcementRepository;
  private final AnnouncementMapper announcementMapper;

  public Page<AnnouncementResponse> searchAnnouncements(Pageable pageable) {
    Page<Announcement> announcementPage = announcementRepository.findAll(pageable);
    return announcementPage.map(announcementMapper::toResponse);
  }
}
