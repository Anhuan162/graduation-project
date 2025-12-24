package com.graduation.project.announcement.mapper;

import com.graduation.project.announcement.dto.AnnouncementResponse;
import com.graduation.project.announcement.dto.AnnouncementTargetResponse;
import com.graduation.project.announcement.dto.FullAnnouncementResponse;
import com.graduation.project.announcement.entity.Announcement;
import com.graduation.project.announcement.entity.AnnouncementTarget;
import java.util.List;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.Named;

@Mapper(componentModel = "spring")
public interface AnnouncementMapper {

  @Mapping(target = "createdBy", expression = "java(entity.getCreatedBy() != null ? entity.getCreatedBy().getFullName() : \"System\")")
  @Mapping(target = "modifiedBy", expression = "java(entity.getModifiedBy() != null ? entity.getModifiedBy().getFullName() : null)")
  AnnouncementResponse toResponse(Announcement entity);

  @Mapping(target = "createdByFullName", expression = "java(entity.getCreatedBy() != null ? entity.getCreatedBy().getFullName() : \"System\")")
  @Mapping(target = "modifiedByFullName", expression = "java(entity.getModifiedBy() != null ? entity.getModifiedBy().getFullName() : null)")
  @Mapping(source = "targets", target = "announcementTargetResponses", qualifiedByName = "mapTargets")
  FullAnnouncementResponse toFullResponse(Announcement entity);

  @Named("mapTargets")
  default List<AnnouncementTargetResponse> mapTargets(List<AnnouncementTarget> targets) {
    if (targets == null || targets.isEmpty())
      return List.of();
    return targets.stream()
        .map(
            t -> AnnouncementTargetResponse.builder()
                .id(t.getId())
                .classroomCode(t.getClassroomCode())
                .build())
        .toList();
  }
}
