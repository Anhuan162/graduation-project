package com.graduation.project.admin.dto;

import com.graduation.project.common.entity.Annoucement;
import java.time.LocalDate;
import java.util.UUID;
import lombok.Builder;
import lombok.Value;

@Value
@Builder
public class UpdatedAnnoucementResponse {
  UUID id;
  String title;
  String content;
  String annoucementType;
  Boolean annoucementStatus;
  String modifiedBy;
  LocalDate modifiedDate;

  public static UpdatedAnnoucementResponse from(Annoucement annoucement) {
    return UpdatedAnnoucementResponse.builder()
        .id(annoucement.getId())
        .title(annoucement.getTitle())
        .content(annoucement.getContent())
        .modifiedBy(annoucement.getModifiedBy().getFullName())
        .modifiedDate(annoucement.getModifiedDate())
        .annoucementStatus(annoucement.getAnnoucementStatus())
        .build();
  }
}
