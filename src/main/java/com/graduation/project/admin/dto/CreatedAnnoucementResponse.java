package com.graduation.project.admin.dto;

import com.graduation.project.common.entity.Annoucement;
import java.time.LocalDate;
import java.util.UUID;
import lombok.Builder;
import lombok.Value;

@Value
@Builder
public class CreatedAnnoucementResponse {
  UUID id;
  String title;
  String content;
  String annoucementType;
  String createdBy;
  LocalDate createdDate;

  public static CreatedAnnoucementResponse from(Annoucement annoucement) {
    return CreatedAnnoucementResponse.builder()
        .id(annoucement.getId())
        .title(annoucement.getTitle())
        .content(annoucement.getContent())
        .createdBy(annoucement.getCreatedBy().getFullName())
        .createdDate(annoucement.getCreatedDate())
        .build();
  }
}
