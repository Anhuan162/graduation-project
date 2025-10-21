package com.graduation.project.user.service;

import com.graduation.project.common.repository.AnnoucementRepository;
import com.graduation.project.user.dto.AnnoucementResponse;
import com.graduation.project.user.mapper.AnnoucementMapper;
import java.util.List;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.springframework.stereotype.Service;

@RequiredArgsConstructor
@Service
@Log4j2
public class AnnoucementService {
  private final AnnoucementRepository annoucementRepository;
  private final AnnoucementMapper annoucementMapper;

  public List<AnnoucementResponse> getAllAnnoucements() {
    return annoucementMapper.toResponseList(annoucementRepository.findAll());
  }
}
