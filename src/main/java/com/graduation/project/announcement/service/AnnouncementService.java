package com.graduation.project.announcement.service;

import com.graduation.project.announcement.repository.AnnouncementRepository;
import com.graduation.project.announcement.dto.FullAnnouncementResponse;
import com.graduation.project.announcement.mapper.AnnouncementMapper;
import java.util.List;

import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.springframework.stereotype.Service;

@RequiredArgsConstructor
@Service
@Log4j2
public class AnnouncementService {
  private final AnnouncementRepository announcementRepository;
  private final AnnouncementMapper announcementMapper;

  public List<FullAnnouncementResponse> getAllAnnouncements() {
    return announcementMapper.toResponseList(announcementRepository.findAll());
  }
}
