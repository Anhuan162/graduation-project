package com.graduation.project.admin.dto;

import com.graduation.project.common.entity.Annoucement;
import com.graduation.project.common.entity.AnnoucementType;
import com.graduation.project.common.entity.User;
import java.time.LocalDate;
import java.util.List;
import java.util.UUID;
import lombok.Builder;
import lombok.Value;

@Value
@Builder
public class UpdatedAnnoucementRequest {
  String title;
  Boolean annoucementStatus;
  String content;
  String annoucementType;
  List<UUID> facultyIds;
  List<String> classCodes;
  List<String> schoolYearCodes;

  public static Annoucement toAnnoucement(UpdatedAnnoucementRequest request, User user) {
    return Annoucement.builder()
        .id(UUID.randomUUID())
        .title(request.getTitle())
        .content(request.getContent())
        .createdBy(user)
        .createdDate(LocalDate.now())
        .build();
  }
}
