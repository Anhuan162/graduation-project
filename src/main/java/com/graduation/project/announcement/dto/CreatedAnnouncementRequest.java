package com.graduation.project.announcement.dto;

import com.graduation.project.announcement.entity.Announcement;
import com.graduation.project.announcement.constant.AnnouncementType;
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

  public static Announcement toAnnouncement(CreatedAnnouncementRequest request, User user) {
    return Announcement.builder()
        .title(request.getTitle())
        .content(request.getContent())
        .announcementType(AnnouncementType.valueOf(request.getAnnouncementType()))
        .createdBy(user)
        .createdDate(LocalDate.now())
        .build();
  }
}
