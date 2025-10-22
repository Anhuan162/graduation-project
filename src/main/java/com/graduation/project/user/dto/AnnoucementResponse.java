package com.graduation.project.user.dto;

import com.graduation.project.common.entity.AnnoucementType;
import java.time.LocalDate;
import java.util.List;
import java.util.UUID;
import lombok.Builder;
import lombok.Value;

@Value
@Builder
public class AnnoucementResponse {
  UUID id;
  String title;
  String content;

  String createdByFullName;
  String modifiedByFullName;

  LocalDate createdDate;
  LocalDate modifiedDate;
  Boolean annoucementStatus;
  AnnoucementType annoucementType;

  List<AnnoucementTargetResponse> annoucementTargetResponses;
}
