package com.graduation.project.user.service;

import com.graduation.project.common.repository.AnnouncementRepository;
import com.graduation.project.user.dto.AnnouncementResponse;
import com.graduation.project.user.mapper.AnnouncementMapper;
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

  public List<AnnouncementResponse> getAllAnnouncements() {
    return announcementMapper.toResponseList(announcementRepository.findAll());
  }
}
