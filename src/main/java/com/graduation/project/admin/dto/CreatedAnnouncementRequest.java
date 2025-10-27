package com.graduation.project.admin.dto;

import com.graduation.project.common.entity.Annoucement;
import com.graduation.project.common.entity.AnnoucementType;
import com.graduation.project.common.entity.User;
import java.time.LocalDate;
import java.util.List;
import java.util.UUID;
import lombok.AllArgsConstructor; // <-- (Nên thêm cho @Builder)
import lombok.Builder;
import lombok.Data; // <-- SỬA TỪ @Value THÀNH @Data
import lombok.NoArgsConstructor; // <-- THÊM CÁI NÀY

@Data // <-- SỬA Ở ĐÂY
@Builder
@NoArgsConstructor // <-- THÊM Ở ĐÂY
@AllArgsConstructor // <-- (Nên thêm cho @Builder)
public class CreatedAnnouncementRequest {
  String title;
  String content;
  String announcementType;
  List<UUID> facultyIds;
  List<String> classCodes;
  List<String> schoolYearCodes;

  public static Annoucement toAnnoucement(CreatedAnnouncementRequest request, User user) {
    return Annoucement.builder()
        .title(request.getTitle())
        .content(request.getContent())
        .annoucementType(AnnoucementType.valueOf(request.getAnnouncementType()))
        .createdBy(user)
        .createdDate(LocalDate.now())
        .build();
  }
}
